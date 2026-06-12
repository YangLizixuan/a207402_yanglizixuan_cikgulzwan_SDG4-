package com.example.a207402_yanglizixuan_cikgulzwan_Project2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.a207402_yanglizixuan_cikgulzwan_Project2.ui.theme.A207402_YangLizixuan_Cikgulzwan_Lab5Theme
import com.example.a207402_yanglizixuan_cikgulzwan_Project2.ui.theme.appAccentColor
import com.example.a207402_yanglizixuan_cikgulzwan_Project2.ui.theme.appPrimaryColor
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.collections.emptyList

//-----------------------网络代码--------------------------
const val BASE_URL = "https://api.quotable.io/"

interface QuoteApi {
    @GET("random")
    suspend fun getRandomQuote(): Quote
}

data class Quote(
    val content: String,
    val author: String
)

object RetrofitClient {
    val api: QuoteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuoteApi::class.java)
    }
}

// ====================== Repository ======================
class DiaryRepository(private val dao: DiaryDao) {
    suspend fun addDiary(content: String) {
        dao.insertDiary(Diary(content = content))
    }
    fun getAllDiaries(): Flow<List<Diary>> = dao.getAllDiaries()
}

// ====================== ViewModel ======================
class DiaryViewModel(
    private val repository: DiaryRepository
) : ViewModel() {
    val allDiaries: Flow<List<Diary>> = repository.getAllDiaries()

    fun saveDiary(content: String) {
        if (content.isNotBlank()) {
            viewModelScope.launch {
                repository.addDiary(content)
            }
        }
    }
}

class DiaryViewModelFactory(
    private val repository: DiaryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            return DiaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

// ====================== 路由 ======================
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Preview : Screen("preview")
    object Profile : Screen("profile")
    object History : Screen("history")
    object Setting : Screen("setting")
    object ApiPage : Screen("api_page")
    object SensorPage : Screen("sensor_page")
}

// ====================== 主活动 ======================
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val granted = permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (!granted) {
            Toast.makeText(this, "需要定位权限", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
        enableEdgeToEdge()
        setContent {
            A207402_YangLizixuan_Cikgulzwan_Lab5Theme {
                val db = DiaryDatabase.getInstance(applicationContext)
                val repo = DiaryRepository(db.diaryDao())
                val factory = DiaryViewModelFactory(repo)
                val viewModel: DiaryViewModel = viewModel(factory = factory)
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(navController, viewModel , firestore)
                    }
                    composable(Screen.Preview.route) {
                        PreviewScreen(navController, viewModel)
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(navController)
                    }
                    composable(Screen.History.route) {
                        HistoryScreen(navController, viewModel)
                    }
                    composable(Screen.Setting.route) {
                        SettingScreen(navController)
                    }
                    composable(Screen.ApiPage.route) {
                        ApiDataScreen(navController)
                    }
                    composable(Screen.SensorPage.route) {
                        SensorGpsScreen(navController, fusedLocationClient)
                    }
                }
            }
        }
    }
}

// ====================== 首页 ======================
@Composable
fun HomeScreen(navController: NavController, viewModel: DiaryViewModel, firestore: FirebaseFirestore) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "大一",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = appPrimaryColor
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { navController.navigate(Screen.ApiPage.route) }) { Text("网络API") }
                Button(onClick = { navController.navigate(Screen.SensorPage.route) }) { Text("GPS传感器") }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                listOf("作业批改", "搜索答疑", "扫码搜书", "计算器").forEach { title ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = appPrimaryColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title.first().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = appPrimaryColor
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                listOf("VIP特惠", "轻松练", "语文作文", "诗词大赛", "全部").forEach { title ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = appAccentColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title.first().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = appAccentColor
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .animateContentSize(animationSpec = tween(350)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            onClick = { isExpanded = !isExpanded }
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                color = appPrimaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("帮", style = MaterialTheme.typography.titleMedium, color = appPrimaryColor)
                    }
                    Text(
                        text = "我是小帮",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
                if (isExpanded) {
                    Text(
                        text = "你的学习搭子已上线～学习难题我随时接招！",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
                    )
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("五花八门是哪五花哪八门？", Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("哪些数字是“孤独的”？", Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(appAccentColor.copy(alpha = 0.15f))) {
                        Text("古诗词抽奖，每天都有新头像框", Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("作业批改", "AI写作", "小帮伴学", "涨知识").forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = appPrimaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Text(it, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                // 输入行：图标 + 输入框
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📷", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp))
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("发消息或按住说话...", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // 按钮行：发送 + 分享（单独一行，横向均分）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveDiary(inputText)
                            inputText = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("发送")
                    }

                    Button(
                        onClick = {
                            if(inputText.isNotBlank()){
                                scope.launch(Dispatchers.IO) {
                                    val data = hashMapOf("content" to inputText)
                                    firestore.collection("community_notes").add(data).await()
                                    withContext(Dispatchers.Main){
                                        Toast.makeText(context,"已上传至云端社区",Toast.LENGTH_SHORT).show()
                                        inputText = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("分享到云端社区(Firebase)")
                    }
                }
            }
        }

        // 底部导航按钮 不变
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { navController.navigate(Screen.Home.route) }) { Text("首页") }
            Button(onClick = { navController.navigate(Screen.Preview.route) }) { Text("预览") }
            Button(onClick = { navController.navigate(Screen.History.route) }) { Text("历史") }
            Button(onClick = { navController.navigate(Screen.Setting.route) }) { Text("设置") }
            Button(onClick = { navController.navigate(Screen.Profile.route) }) { Text("我的") }
        }
    }
    }

// ====================== 预览页 ======================
@Composable
fun PreviewScreen(
    navController: NavController,
    viewModel: DiaryViewModel
) {
    val diaries by viewModel.allDiaries.collectAsStateWithLifecycle(initialValue = emptyList())
    val last = diaries.firstOrNull()?.content ?: "暂无日记"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📄 消息预览", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("最后输入：", style = MaterialTheme.typography.titleLarge)
                Text(last, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 10.dp))
            }
        }
        Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 20.dp)) {
            Text("返回首页")
        }
    }
}

// ====================== 历史页 ======================
@Composable
fun HistoryScreen(navController: NavController, viewModel: DiaryViewModel) {
    val diaries by viewModel.allDiaries.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📜 历史记录", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        if (diaries.isEmpty()) {
            Text("暂无记录", modifier = Modifier.padding(top = 20.dp))
        } else {
            diaries.forEach { diary ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("ID: ${diary.id}")
                        Text("内容: ${diary.content}")
                    }
                }
            }
        }

        Button(onClick = { navController.navigate(Screen.Home.route) }, modifier = Modifier.padding(top = 20.dp)) {
            Text("返回首页")
        }
    }
}

// ====================== 个人 ======================
@Composable
fun ProfileScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👤 个人中心", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Button(onClick = { navController.navigate(Screen.Home.route) }) {
            Text("回到首页")
        }
    }
}

// ====================== 设置 ======================
@Composable
fun SettingScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚙️ 设置页面", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Button(onClick = { navController.navigate(Screen.Home.route) }) {
            Text("返回首页")
        }
    }
}

//---------------网络 API 页面----------------------
@Composable
fun ApiDataScreen(navController: NavController) {
    var quoteText by remember { mutableStateOf("点击按钮加载网络数据") }
    var authorText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🌐 网络API数据(Retrofit)", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(quoteText, style = MaterialTheme.typography.bodyLarge)
                Text(authorText, modifier = Modifier.padding(top = 8.dp), color = Color.Gray)
            }
        }

        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                try {
                    val result = RetrofitClient.api.getRandomQuote()
                    withContext(Dispatchers.Main) {
                        quoteText = result.content
                        authorText = "—— ${result.author}"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        quoteText = "Network request failed. Please check your network connection."
                        authorText = ""
                    }
                }
            }
        }) {
            Text("加载随机名言(API)")
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text("返回首页")
        }
    }
}

//-------------------------------GPS 传感器页面--------------------------------------
@Composable
fun SensorGpsScreen(
    navController: NavController,
    locationClient: FusedLocationProviderClient
) {
    var locationInfo by remember { mutableStateOf("点击按钮获取GPS定位") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📍 GPS 定位传感器", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = locationInfo,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Button(onClick = {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                locationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        locationInfo = "纬度：${it.latitude}\n经度：${it.longitude}"
                    } ?: run {
                        locationInfo = "无法获取定位，请开启手机定位"
                    }
                }
            } else {
                locationInfo = "定位权限未开启"
            }
        }) {
            Text("获取当前位置")
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text("返回首页")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    A207402_YangLizixuan_Cikgulzwan_Lab5Theme {
        val nav = rememberNavController()
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val db = DiaryDatabase.getInstance(ctx)
        val repo = DiaryRepository(db.diaryDao())
        val vm: DiaryViewModel = viewModel(factory = DiaryViewModelFactory(repo))
        HomeScreen(nav, vm, FirebaseFirestore.getInstance())
    }
}