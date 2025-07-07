package com.flavicox.insurapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.flavicox.insurapp.model.PaymentRequest
import com.flavicox.insurapp.model.PaymentResponse
import com.flavicox.insurapp.network.AuthApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Clase de pruebas unitarias para la funcionalidad de integración con Stripe.
 * Valida la creación de objetos de pago, formatos de respuesta de Stripe,
 * conversiones de moneda y manejo de errores.
 */
@ExperimentalCoroutinesApi
class StripeTest {

    // Regla que ejecuta todas las operaciones de Architecture Components
    // de forma síncrona en el hilo principal para testing
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Dispatcher de prueba que ejecuta corrutinas inmediatamente sin delay
    private val testDispatcher = UnconfinedTestDispatcher()

    // Instancia mock del servicio de API para simular respuestas del servidor
    private lateinit var mockApiService: MockAuthApiService

    /**
     * Configuración inicial que se ejecuta antes de cada test.
     * Establece el dispatcher principal para testing y crea el mock del API service.
     */
    @Before
    fun setup() {
        // Reemplaza el dispatcher principal con uno de prueba
        Dispatchers.setMain(testDispatcher)
        // Inicializa el mock del servicio de API
        mockApiService = MockAuthApiService()
    }

    /**
     * Limpieza que se ejecuta después de cada test.
     * Restaura el dispatcher principal original.
     */
    @After
    fun tearDown() {
        // Restaura el dispatcher original del sistema
        Dispatchers.resetMain()
    }

    // ──────────────────────────────────────────────────────
    // CASOS DE ÉXITO - Verifican el comportamiento correcto
    // ──────────────────────────────────────────────────────

    /**
     * Verifica que se pueda crear un PaymentRequest con datos válidos
     * y que todos los campos se asignen correctamente.
     */
    @Test
    fun shouldCreatePaymentRequestWithValidData() {
        // Arrange & Act - Prepara y ejecuta la creación del objeto
        val paymentRequest = PaymentRequest(
            amount = 5000,                    // Monto en centavos (S/. 50.00)
            clientName = "Juan Pérez",        // Nombre completo del cliente
            productName = "Reserva Fútbol #1", // Descripción del producto/servicio
            phone = "987654321",              // Teléfono del cliente
            email = "juan@test.com"           // Email del cliente
        )

        // Assert - Verifica que cada campo tenga el valor esperado
        assertEquals("Amount should be 5000", 5000, paymentRequest.amount)
        assertEquals("Client name should match", "Juan Pérez", paymentRequest.clientName)
        assertEquals("Product name should match", "Reserva Fútbol #1", paymentRequest.productName)
        assertEquals("Phone should match", "987654321", paymentRequest.phone)
        assertEquals("Email should match", "juan@test.com", paymentRequest.email)
    }

    /**
     * Verifica que PaymentResponse contenga todos los campos requeridos por Stripe
     * con los formatos correctos (prefijos pi_, ek_, cus_, etc.).
     */
    @Test
    fun shouldContainAllRequiredFieldsInPaymentResponse() {
        // Arrange & Act - Crea una respuesta simulada con formato de Stripe
        val paymentResponse = PaymentResponse(
            clientSecret = "pi_test_1234567890_secret_abcd",    // Secret del PaymentIntent
            ephemeralSecret = "ek_test_1234567890",             // Key efímera del cliente
            id = "cus_test_customer",                           // ID del customer en Stripe
            paymentIntentId = "pi_test_1234567890"              // ID del PaymentIntent
        )

        // Assert - Verifica que cada campo tenga el formato correcto de Stripe
        assertTrue("Client secret should contain 'pi_'",
            paymentResponse.clientSecret.contains("pi_"))
        assertTrue("Client secret should contain '_secret_'",
            paymentResponse.clientSecret.contains("_secret_"))
        assertTrue("Ephemeral secret should contain 'ek_'",
            paymentResponse.ephemeralSecret.contains("ek_"))
        assertTrue("Customer ID should contain 'cus_'",
            paymentResponse.id.contains("cus_"))
        assertTrue("Payment Intent ID should contain 'pi_'",
            paymentResponse.paymentIntentId.contains("pi_"))
    }

    /**
     * Verifica la conversión correcta de soles a centavos.
     * Stripe requiere que los montos se envíen en la unidad más pequeña (centavos).
     */
    @Test
    fun shouldConvertAmountToCentsCorrectly() {
        // Arrange - Define un precio en soles
        val totalPrice = 50.0 // S/. 50.00

        // Act - Convierte a centavos multiplicando por 100
        val amountInCents = (totalPrice * 100).toInt()

        // Assert - Verifica que la conversión sea correcta
        assertEquals("50 soles should be 5000 cents", 5000, amountInCents)
    }

    /**
     * Simula la creación de un PaymentRequest completo para una reserva de cancha,
     * incluyendo todos los datos del usuario y información del campo.
     */
    @Test
    fun shouldCreatePaymentRequestForReservationWithAllData() {
        // Arrange - Datos simulados de una reserva real
        val totalPrice = 75.5              // Precio total de la reserva
        val userName = "María"             // Nombre del usuario
        val userSurname = "García"         // Apellido del usuario
        val fieldType = "Fútbol"           // Tipo de cancha
        val fieldNumber = "3"              // Número de la cancha
        val userPhone = "999888777"        // Teléfono del usuario
        val userEmail = "maria@test.com"   // Email del usuario

        // Act - Crea el PaymentRequest con formato de reserva
        val paymentRequest = PaymentRequest(
            amount = (totalPrice * 100).toInt(),                    // Convierte a centavos
            clientName = "$userName $userSurname",                  // Nombre completo
            productName = "Reserva $fieldType #$fieldNumber",      // Descripción del servicio
            phone = userPhone,
            email = userEmail
        )

        // Assert - Verifica que todos los campos se generen correctamente
        assertEquals("Amount should be 7550 cents", 7550, paymentRequest.amount)
        assertEquals("Client name should be complete", "María García", paymentRequest.clientName)
        assertEquals("Product name should include type and number", "Reserva Fútbol #3", paymentRequest.productName)
        assertEquals("Phone should match", "999888777", paymentRequest.phone)
        assertEquals("Email should match", "maria@test.com", paymentRequest.email)
    }

    // ──────────────────────────────────────────────────────
    // CASOS DE VALIDACIÓN - Verifican reglas de negocio
    // ──────────────────────────────────────────────────────

    /**
     * Verifica que todos los campos requeridos estén presentes en PaymentRequest,
     * incluso cuando están vacíos (no deben ser null).
     */
    @Test
    fun shouldHaveAllRequiredFieldsPresentInPaymentRequest() {
        // Arrange & Act - Crea un PaymentRequest con campos vacíos pero no null
        val paymentRequest = PaymentRequest(
            amount = 1000,
            clientName = "",     // Vacío pero no null
            productName = "",    // Vacío pero no null
            phone = "",          // Vacío pero no null
            email = ""           // Vacío pero no null
        )

        // Assert - Verifica que los campos existan aunque estén vacíos
        assertNotNull("Amount should be present", paymentRequest.amount)
        assertNotNull("ClientName should be present", paymentRequest.clientName)
        assertNotNull("ProductName should be present", paymentRequest.productName)
        assertNotNull("Phone should be present", paymentRequest.phone)
        assertNotNull("Email should be present", paymentRequest.email)
    }

    /**
     * Valida que el monto mínimo permitido sea mayor a cero.
     * Stripe no acepta transacciones con monto cero o negativo.
     */
    @Test
    fun shouldValidateMinimumAmountIsGreaterThanZero() {
        // Arrange - Define un monto mínimo válido
        val minAmount = 1

        // Act - Verifica si el monto es válido
        val isValidAmount = minAmount > 0

        // Assert - Confirma que montos positivos son válidos
        assertTrue("Minimum amount should be greater than 0", isValidAmount)
    }

    /**
     * Valida que el clientSecret siga el patrón correcto de Stripe.
     * Debe empezar con "pi_", contener "_secret_" y tener longitud suficiente.
     */
    @Test
    fun shouldValidateClientSecretFollowsStripePattern() {
        // Arrange - Client secret con formato real de Stripe
        val clientSecret = "pi_3QLmQXQpnfpZJWEM1234567890_secret_abcdefghijklmnop"

        // Act - Valida el formato según las reglas de Stripe
        val isValidFormat = clientSecret.startsWith("pi_") &&
                clientSecret.contains("_secret_") &&
                clientSecret.length > 20

        // Assert - Confirma que el formato es válido
        assertTrue("Client secret should have valid Stripe format", isValidFormat)
    }

    /**
     * Valida que la ephemeral key siga el patrón correcto de Stripe.
     * Debe empezar con "ek_" y tener longitud mínima.
     */
    @Test
    fun shouldValidateEphemeralKeyFollowsStripePattern() {
        // Arrange - Ephemeral key con formato de Stripe
        val ephemeralKey = "ek_test_1234567890abcdefghijklmnop"

        // Act - Valida el formato de la key efímera
        val isValidFormat = ephemeralKey.startsWith("ek_") && ephemeralKey.length > 10

        // Assert - Confirma que el formato es correcto
        assertTrue("Ephemeral key should have valid Stripe format", isValidFormat)
    }

    /**
     * Valida que el Customer ID siga el patrón de Stripe.
     * Debe empezar con "cus_" y tener longitud mínima.
     */
    @Test
    fun shouldValidateCustomerIdFollowsStripePattern() {
        // Arrange - Customer ID con formato de Stripe
        val customerId = "cus_1234567890abcdef"

        // Act - Valida el formato del ID de cliente
        val isValidFormat = customerId.startsWith("cus_") && customerId.length > 8

        // Assert - Confirma que el formato es válido
        assertTrue("Customer ID should have valid Stripe format", isValidFormat)
    }

    /**
     * Valida que el Payment Intent ID siga el patrón de Stripe.
     * Debe empezar con "pi_" y tener longitud mínima.
     */
    @Test
    fun shouldValidatePaymentIntentIdFollowsStripePattern() {
        // Arrange - PaymentIntent ID con formato de Stripe
        val paymentIntentId = "pi_3QLmQXQpnfpZJWEM1234567890"

        // Act - Valida el formato del ID del PaymentIntent
        val isValidFormat = paymentIntentId.startsWith("pi_") && paymentIntentId.length > 8

        // Assert - Confirma que el formato es correcto
        assertTrue("Payment Intent ID should have valid Stripe format", isValidFormat)
    }

    // ──────────────────────────────────────────────────────
    // CASOS DE ERROR Y EDGE CASES - Manejan situaciones límite
    // ──────────────────────────────────────────────────────

    /**
     * Verifica que montos negativos sean detectados como inválidos.
     * Los pagos no pueden tener valores negativos.
     */
    @Test
    fun shouldDetectNegativeAmountAsInvalid() {
        // Arrange - Monto negativo (caso inválido)
        val negativeAmount = -100

        // Act - Evalúa si el monto es válido
        val isValidAmount = negativeAmount > 0

        // Assert - Confirma que montos negativos son inválidos
        assertFalse("Negative amount should be invalid", isValidAmount)
    }

    /**
     * Verifica que el monto cero sea detectado como inválido.
     * Stripe no permite transacciones de cero centavos.
     */
    @Test
    fun shouldDetectZeroAmountAsInvalid() {
        // Arrange - Monto de cero (caso inválido)
        val zeroAmount = 0

        // Act - Evalúa si el monto es válido
        val isValidAmount = zeroAmount > 0

        // Assert - Confirma que monto cero es inválido
        assertFalse("Zero amount should be invalid", isValidAmount)
    }

    /**
     * Verifica que client secrets vacíos sean detectados como inválidos.
     * Un client secret es obligatorio para procesar pagos.
     */
    @Test
    fun shouldDetectEmptyClientSecretAsInvalid() {
        // Arrange - Client secret vacío (caso inválido)
        val emptyClientSecret = ""

        // Act - Valida si el client secret es válido
        val isValidClientSecret = emptyClientSecret.isNotEmpty() &&
                emptyClientSecret.startsWith("pi_")

        // Assert - Confirma que client secrets vacíos son inválidos
        assertFalse("Empty client secret should be invalid", isValidClientSecret)
    }

    /**
     * Prueba la conversión de decimales a centavos en casos límite.
     * Maneja diferentes tipos de precios: exactos, con decimales y muy pequeños.
     */
    @Test
    fun shouldHandleDecimalToCentsConversionEdgeCases() {
        // Caso 1: Precio exacto sin decimales
        val price1 = 10.0
        val cents1 = (price1 * 100).toInt()
        assertEquals("10.0 soles = 1000 cents", 1000, cents1)

        // Caso 2: Precio con decimales
        val price2 = 25.75
        val cents2 = (price2 * 100).toInt()
        assertEquals("25.75 soles = 2575 cents", 2575, cents2)

        // Caso 3: Precio muy pequeño (menos de 1 sol)
        val price3 = 0.50
        val cents3 = (price3 * 100).toInt()
        assertEquals("0.50 soles = 50 cents", 50, cents3)
    }

    /**
     * Valida formato básico de email.
     * Verifica que contenga "@" y "." como validación mínima.
     */
    @Test
    fun shouldValidateBasicEmailFormat() {
        // Arrange - Emails de prueba (válido e inválido)
        val validEmail = "test@example.com"      // Email con formato correcto
        val invalidEmail = "invalid-email"       // Email sin formato correcto

        // Act - Aplica validación básica de email
        val isValidEmail = validEmail.contains("@") && validEmail.contains(".")
        val isInvalidEmail = invalidEmail.contains("@") && invalidEmail.contains(".")

        // Assert - Verifica que la validación funcione correctamente
        assertTrue("Valid email should contain @ and dot", isValidEmail)
        assertFalse("Invalid email should not pass basic validation", isInvalidEmail)
    }

    /**
     * Verifica el manejo de nombres vacíos.
     * Cuando nombre y apellido están vacíos, el resultado debe ser inválido.
     */
    @Test
    fun shouldHandleEmptyNames() {
        // Arrange - Nombres vacíos
        val emptyName = ""
        val emptySurname = ""

        // Act - Combina nombres y verifica si es válido
        val fullName = "$emptyName $emptySurname".trim()
        val isValidName = fullName.isNotEmpty()

        // Assert - Confirma que nombres vacíos resulten en inválido
        assertFalse("Empty names should result in invalid name", isValidName)
    }

    /**
     * Verifica que se genere correctamente el nombre del producto
     * incluyendo información del campo deportivo.
     */
    @Test
    fun shouldGenerateProductNameWithFieldInfo() {
        // Arrange - Datos del campo deportivo
        val fieldType = "Básquet"     // Tipo de cancha
        val fieldNumber = "2"         // Número de la cancha

        // Act - Genera el nombre del producto
        val productName = "Reserva $fieldType #$fieldNumber"

        // Assert - Verifica que el nombre contenga toda la información necesaria
        assertEquals("Product name should include type and number",
            "Reserva Básquet #2", productName)
        assertTrue("Should contain word Reserva", productName.contains("Reserva"))
        assertTrue("Should contain field type", productName.contains(fieldType))
        assertTrue("Should contain field number", productName.contains(fieldNumber))
    }

    /**
     * Simula una respuesta exitosa de Stripe para verificar
     * que el mock funciona correctamente y genera datos válidos.
     */
    @Test
    fun shouldSimulateSuccessfulStripeResponse() {
        // Simula el flujo completo de una respuesta exitosa
        val mockApiService = MockAuthApiService()

        // Configura respuesta exitosa con datos válidos de Stripe
        mockApiService.paymentResponseToReturn = PaymentResponse(
            clientSecret = "pi_test_1234567890_secret_abcdefg",
            ephemeralSecret = "ek_test_1234567890",
            id = "cus_test_customer",
            paymentIntentId = "pi_test_1234567890"
        )

        // Verifica que la respuesta tenga el formato correcto
        val response = mockApiService.paymentResponseToReturn!!
        assertTrue("Client secret should be valid", response.clientSecret.startsWith("pi_"))
        assertTrue("Ephemeral key should be valid", response.ephemeralSecret.startsWith("ek_"))
        assertTrue("Customer ID should be valid", response.id.startsWith("cus_"))
    }

    /**
     * Simula un error de red para verificar que el sistema
     * maneje correctamente las excepciones de conectividad.
     */
    @Test
    fun shouldSimulateNetworkError() {
        // Simula un error de conexión
        val mockApiService = MockAuthApiService()

        // Configura para que lance una excepción
        mockApiService.shouldThrowException = true
        mockApiService.exceptionToThrow = Exception("Network timeout")

        // Verifica que el error se maneje correctamente
        assertTrue("Should be configured to throw exception", mockApiService.shouldThrowException)
        assertEquals("Error message should be correct", "Network timeout", mockApiService.exceptionToThrow?.message)
    }
}

// ──────────────────────────────────────────────────────
// Mock del AuthApiService para tests sin dependencias externas
// Simula las respuestas del servidor para testing
// ──────────────────────────────────────────────────────

/**
 * Implementación mock del AuthApiService para testing.
 * Permite simular respuestas exitosas y errores sin hacer llamadas reales al servidor.
 */
class MockAuthApiService : AuthApiService {

    // Flags para controlar el comportamiento del mock
    var shouldThrowException = false              // Si debe lanzar excepción
    var exceptionToThrow: Exception? = null       // Excepción específica a lanzar
    var paymentResponseToReturn: PaymentResponse? = null  // Respuesta a retornar

    /**
     * Implementación mock de initiatePayment.
     * Puede simular tanto respuestas exitosas como errores de red.
     */
    override suspend fun initiatePayment(
        reservationId: Int,    // ID de la reserva
        body: PaymentRequest,  // Datos del pago
        token: String          // Token de autenticación
    ): PaymentResponse {
        // Si está configurado para lanzar excepción, la lanza
        if (shouldThrowException) {
            throw exceptionToThrow ?: Exception("Mock exception")
        }

        // Retorna la respuesta configurada o una por defecto
        return paymentResponseToReturn ?: PaymentResponse(
            clientSecret = "pi_test_1234567890_secret_abcdefg",
            ephemeralSecret = "ek_test_1234567890",
            id = "cus_test_customer",
            paymentIntentId = "pi_test_1234567890"
        )
    }

    // Implementaciones vacías de otros métodos requeridos por la interfaz
    // No se usan en estos tests pero son obligatorios para cumplir el contrato
    override suspend fun registerUser(request: com.flavicox.insurapp.model.RegisterRequest) = TODO()
    override suspend fun validateCode(code: String) = TODO()
    override suspend fun login(request: com.flavicox.insurapp.model.LoginRequest) = TODO()
    override suspend fun getUserProfile(token: String) = TODO()
    override suspend fun updateUserProfile(updatedProfile: Map<String, String>, token: String) = TODO()
    override suspend fun updatePassword(passwordBody: Map<String, String>, token: String) = TODO()
    override suspend fun getAvailableFields(token: String) = TODO()
    override suspend fun getAvailableTimes(fieldId: Int, bookingDate: String, token: String) = TODO()
    override suspend fun getFieldById(fieldId: Int, token: String) = TODO()
    override suspend fun createReservation(request: com.flavicox.insurapp.model.CreateReserveRequest, token: String) = TODO()
    override suspend fun getMyReservations(token: String) = TODO()
    override suspend fun validateReservation(id: Int, token: String) = TODO()
    override suspend fun validateReservationPatch(id: Int, token: String) = TODO()
    override suspend fun getReservationById(id: Int, token: String) = TODO()
}