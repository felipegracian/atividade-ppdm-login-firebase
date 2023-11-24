package com.example.ppdmatividade

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.ppdmatividade.ui.theme.PPDMAtividadeTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.ppdmatividade.repository.LoginRepository
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.ppdmatividade.service.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PPDMAtividadeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PPDMActivity(lifecycleScope = lifecycleScope)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PPDMActivity(
    lifecycleScope: LifecycleCoroutineScope
) {

    val context = LocalContext.current

    val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child("images")

    val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    var fotoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context).data(fotoUri).build()
    )

    var isImageSelected by remember { mutableStateOf(false) }

    var selectedMedia by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fotoUri = it
            selectedMedia = selectedMedia + listOf(it)
        }
    }

    var emailState by remember {
        mutableStateOf("")
    }

    var passwordState by remember{
        mutableStateOf("")
    }

    var passwordVisibilityState by remember {
        mutableStateOf(false)
    }


    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        verticalArrangement = Arrangement.Center
    ) {

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .height(600.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ){

            OutlinedTextField(
                value = emailState,
                onValueChange = {
                    emailState = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight(400)),
                label = { Text(text = "Login" )},
                shape = RoundedCornerShape(16.dp),
            )

            OutlinedTextField(
                value = passwordState,
                onValueChange = {
                    passwordState = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight(400)),
                label = { Text(text = "Senha" )},
                shape = RoundedCornerShape(16.dp),
            )

            if(fotoUri == null){
                Image(
                    painter = painterResource(id = R.drawable.profile)
                    , contentDescription = "",
                    modifier = Modifier.clickable {
                        launcher.launch("image/*")
                    }
                )
            } else {

                Card (modifier = Modifier.height(250.dp).width(250.dp)) {
                    AsyncImage(
                        model = fotoUri, contentDescription = "",
                        modifier = Modifier.clickable {
                            launcher.launch("image/*")
                        })
                }

            }

            

            Button(
                onClick = {
                    if (selectedMedia.isNotEmpty()) {
                        for (uri in selectedMedia) {
                            val storageRef = storageRef.child("${uri.lastPathSegment}-${System.currentTimeMillis()}.jpg")
                            storageRef.putFile(uri).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                        val map = hashMapOf("pic" to downloadUri.toString())
                                        firebaseFirestore.collection("images").add(map).addOnCompleteListener { firestoreTask ->
                                            if (firestoreTask.isSuccessful) {
                                                if (selectedMedia.last() == uri) {
                                                    val loginRepository = LoginRepository()


                                                    Log.e("Valores", "${emailState}")
                                                    Log.e("Valores", "${passwordState}")
                                                    Log.e("Valores", "${downloadUri.toString()}")


                                                    lifecycleScope.launch {

                                                        val response = loginRepository.loginUsuario(emailState, passwordState, downloadUri.toString())

                                                        Log.e("Response", "${response.body()}")
                                                        Log.e("Response", "${response.message()}")
                                                        Log.e("Response", "${response.code()}")


                                                        if(response.isSuccessful){
                                                            Log.e("Deu certooo", "haa")
                                                        } else{
                                                            Log.e("Para de ser burro", "Macaco Burro")
                                                        }


                                                    }

                                                }
                                            } else {
                                                Toast.makeText(context, "ERRO AO TENTAR REALIZAR O UPLOAD", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "ERRO AO TENTAR REALIZAR O UPLOAD", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Selecione ao menos 1 imagem para prosseguir", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(text = "Entrar")
            }

        }
    }
}

