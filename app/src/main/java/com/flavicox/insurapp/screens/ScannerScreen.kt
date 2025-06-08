package com.flavicox.insurapp.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(navController: NavController) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var scannedText by remember { mutableStateOf<String?>(null) }


    //Para guardar el nombre de usuario del TopBarCampos (HEADER)
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val fullName by viewModel.userFullNameFlow.collectAsState(initial = "")

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    // Cuando cambie scannedText, intentamos extraer el parámetro "id" con Uri.parse
    scannedText?.let { qrContent ->
        // Limpiamos scannedText de inmediato para que no vuelva a dispararse múltiples veces
        scannedText = null

        try {
            // Reemplazamos cualquier "\=" accidental por "=" antes de parsear
            val sanitized = qrContent.replace("\\=", "=")
            val uri = Uri.parse(sanitized)
            val idParam = uri.getQueryParameter("id")
            val id = idParam?.toIntOrNull()

            if (id != null) {
                Log.d("SCANNER", "Navegando a validate_reservation/$id")
                navController.navigate("validate_reservation/$id")
            } else {
                Log.e("SCANNER", "No se encontró un parámetro id válido en: $qrContent")
            }
        } catch (e: Exception) {
            Log.e("SCANNER", "Error al parsear la URL del QR: $qrContent", e)
        }
    }

    Column {
        TopBarCampos(
            nombreUsuario = fullName,
            onProfileClick = {
                navController.navigate(AppScreens.ProfileScreen.route)
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ){
            Box (modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp))
            {
                BotonRegresar(navController)
                Titulo("Escanear QR")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Escanea el código QR para validar la reserva.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (cameraPermissionState.status.isGranted) {
                    CameraPreviewWithScan { result ->
                        Log.d("SCANNER", "ML Kit DETECTÓ: $result")
                        scannedText = result
                    }
                } else {
                    Text(
                        "Se necesita permiso de cámara",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

    }
}

@Composable
fun CameraPreviewWithScan(onScanResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = androidx.camera.core.Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val analyzer = ImageAnalysis.Builder().build().apply {
            setAnalyzer(executor) { imageProxy ->
                processImageProxy(imageProxy, onScanResult)
            }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analyzer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun processImageProxy(imageProxy: ImageProxy, onScanResult: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    val scanner = BarcodeScanning.getClient()

    Log.d("SCANNER", "processImageProxy(): enviando imagen a ML Kit")

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
                barcodes.forEach { barcode ->
                    barcode.rawValue?.let { rawValue ->
                        Log.d("SCANNER", "ML Kit DETECTÓ (rawValue): $rawValue")
                        onScanResult(rawValue)
                    }
                }
            } else {
                Log.d("SCANNER", "ML Kit NO encontró ningún código en este frame")
            }
        }
        .addOnFailureListener { e ->
            Log.e("SCANNER", "processImageProxy(): error ML Kit", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
