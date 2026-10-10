@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.the412banner.bfe.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.the412banner.bfe.BuildConfig
import com.the412banner.bfe.apk.*
import com.the412banner.bfe.clone.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private data class EditorTarget(
    val source: CloneSource,
    val meta: ApkMeta,
    val base: File,
    val splits: List<File>,
)

private fun toast(context: Context, message: String) =
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()

private fun canInstallPackages(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

private fun installSettings(context: Context) {
    val request = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        .setData(Uri.parse("package:" + context.packageName))
    runCatching { context.startActivity(request) }
        .onFailure { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
}

@Composable
private fun CloneIcon(pkg: String, modifier: Modifier = Modifier.size(42.dp)) {
    val context = LocalContext.current
    val bitmap = remember(pkg) {
        runCatching {
            context.packageManager.getApplicationIcon(pkg).toBitmap(72, 72).asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
    else Icon(Icons.Default.Android, contentDescription = null, modifier = modifier, tint = MaterialTheme.colorScheme.primary)
}

@Composable
private fun PermissionOnboarding(canInstall: Boolean, onDone: () -> Unit) {
    val context = LocalContext.current
    var notifRefresh by remember { mutableIntStateOf(0) }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifRefresh++
    }
    val hasNotification = remember(notifRefresh) {
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Preparar Clone Hub", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Autorize apenas o necessário para clonar e instalar aplicativos. As autorizações são sempre concedidas nas telas oficiais do Android.")
        Spacer(Modifier.height(20.dp))
        Text("1. Instalar aplicações de fontes desconhecidas", fontWeight = FontWeight.SemiBold)
        Text(
            if (canInstall) "Autorizado" else "Obrigatório para instalar os APKs clonados.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!canInstall) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = { installSettings(context) }) { Text("Autorizar instalação") }
        }
        Spacer(Modifier.height(16.dp))
        Text("2. Notificações do processo", fontWeight = FontWeight.SemiBold)
        Text(
            if (hasNotification) "Autorizado ou não necessário." else "Opcional: para receber avisos enquanto o clone é preparado.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!hasNotification && Build.VERSION.SDK_INT >= 33) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { notifications.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                Text("Autorizar notificações")
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "O Clone Hub utiliza armazenamento privado para gerar APKs. Não é necessário dar acesso a todos os ficheiros do telefone.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(24.dp))
        Button(enabled = canInstall, onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Continuar") }
    }
}

@Composable
private fun AppPickerDialog(
    sources: List<CloneSource>,
    onDismiss: () -> Unit,
    onPick: (CloneSource) -> Unit,
) {
    var filter by remember { mutableStateOf("") }
    val shown = remember(sources, filter) {
        sources.filter {
            filter.isBlank() || it.label.contains(filter, ignoreCase = true) || it.packageName.contains(filter, ignoreCase = true)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clonar aplicativo") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 540.dp)) {
                OutlinedTextField(
                    value = filter, onValueChange = { filter = it },
                    label = { Text("Pesquisar aplicações") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                if (shown.isEmpty()) Text("Nenhum aplicativo baixado encontrado.")
                LazyColumn {
                    items(shown, key = { it.packageName }) { source ->
                        Row(
                            modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { onPick(source) }).padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CloneIcon(source.packageName, Modifier.size(34.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(source.label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                                Text(source.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}

@Composable
private fun GroupModal(
    sourceLabel: String,
    entries: List<CloneEntry>,
    selection: Set<String>,
    pro: Boolean,
    onDismiss: () -> Unit,
    onToggle: (String) -> Unit,
    onOpen: (CloneEntry) -> Unit,
    onInfo: (CloneEntry) -> Unit,
    onUninstall: (CloneEntry) -> Unit,
    onShortcut: (CloneEntry) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(sourceLabel + " · " + entries.size + " clone(s)") },
        text = {
            Column(modifier = Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState())) {
                entries.forEach { entry ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { if (selection.isNotEmpty()) onToggle(entry.clonePackage) else onOpen(entry) },
                            onLongClick = { onToggle(entry.clonePackage) },
                        ),
                        color = if (entry.clonePackage in selection) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                (if (entry.clonePackage in selection) "✓  " else "") + entry.cloneLabel,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )
                            Text(entry.clonePackage, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { onOpen(entry) }) { Text("Abrir") }
                                TextButton(onClick = { onInfo(entry) }) { Text("Info") }
                                TextButton(onClick = { onUninstall(entry) }) { Text("Desinstalar") }
                            }
                            if (pro) TextButton(onClick = { onShortcut(entry) }) { Text("Criar atalho no ecrã inicial") }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    "Pressione e segure um clone para selecionar. A seleção e a lixeira aparecem no painel principal.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}

/**
 * Dedicated Clone Hub. APK generation stays in BFE ApkJobService; the Android installer,
 * package catalog, selection, shortcuts and permissions are owned by this screen.
 */
@Composable
fun CloneHubScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val onboarding = remember {
        context.getSharedPreferences("clone_hub_ui", Context.MODE_PRIVATE)
    }
    var permissionsDone by remember { mutableStateOf(onboarding.getBoolean("onboarded", false)) }
    var refresh by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val canInstall = remember(refresh) { canInstallPackages(context) }
    var pro by remember { mutableStateOf(ClonePlan.isPro(context)) }
    var clones by remember { mutableStateOf<List<CloneEntry>>(emptyList()) }
    LaunchedEffect(refresh) {
        clones = withContext(Dispatchers.IO) { CloneCatalog.reconcile(context) }
    }

    var picker by remember { mutableStateOf(false) }
    var pickerApps by remember { mutableStateOf<List<CloneSource>>(emptyList()) }
    var loading by remember { mutableStateOf<String?>(null) }
    var editor by remember { mutableStateOf<EditorTarget?>(null) }
    var activeGroup by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var uninstallConfirmation by remember { mutableStateOf<List<String>?>(null) }
    var removeHostWithAll by remember { mutableStateOf(false) }
    var awaitingHostRemoval by remember { mutableStateOf(false) }
    var uninstallQueue by remember { mutableStateOf<List<String>>(emptyList()) }
    var pendingJobId by remember { mutableLongStateOf(0L) }
    var pendingJobEntry by remember { mutableStateOf<CloneEntry?>(null) }
    var pendingInstallerPkg by remember { mutableStateOf<String?>(null) }
    var pendingInstallerFile by remember { mutableStateOf<String?>(null) }
    var showKeys by remember { mutableStateOf(false) }

    val installer = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val pkg = pendingInstallerPkg
        if (pkg != null) {
            val installed = CloneCatalog.confirmInstallerReturn(context, pkg)
            toast(context, if (installed) "Clone instalado com sucesso." else "Instalação não confirmada ou cancelada.")
            pendingInstallerPkg = null
            pendingInstallerFile?.let { path -> runCatching { File(path).delete() } }
            pendingInstallerFile = null
            refresh++
        }
    }

    val remover = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val pkg = uninstallQueue.firstOrNull()
        if (pkg != null) {
            val removed = CloneCatalog.dropIfAbsent(context, pkg)
            if (!removed) toast(context, "Desinstalação não confirmada: " + pkg)
            uninstallQueue = uninstallQueue.drop(1)
            refresh++
        }
    }

    LaunchedEffect(uninstallQueue) {
        val next = uninstallQueue.firstOrNull()
        if (next != null) {
            runCatching {
                remover.launch(Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + next)))
            }.onFailure {
                toast(context, "Não foi possível abrir a desinstalação: " + next)
                uninstallQueue = uninstallQueue.drop(1)
            }
        }
    }

    LaunchedEffect(uninstallQueue, awaitingHostRemoval, refresh) {
        if (awaitingHostRemoval && uninstallQueue.isEmpty()) {
            awaitingHostRemoval = false
            if (CloneCatalog.installed(context).isEmpty()) {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + context.packageName)))
                }.onFailure { toast(context, "Não foi possível abrir a desinstalação do Clone Hub.") }
            } else {
                toast(context, "Há clones instalados. O Clone Hub não será desinstalado enquanto existirem.")
            }
        }
    }

    val jobState by ApkJobManager.state.collectAsState()
    LaunchedEffect(jobState.jobId, jobState.stage) {
        val record = pendingJobEntry
        if (pendingJobId == 0L || jobState.jobId != pendingJobId || record == null) return@LaunchedEffect
        when (jobState.stage) {
            ApkJobStage.DONE -> {
                pendingJobId = 0L
                pendingJobEntry = null
                val report = jobState.report
                if (report == null || !report.verified || report.packageName != record.clonePackage) {
                    toast(context, "O APK gerado não corresponde à instalação solicitada.")
                    return@LaunchedEffect
                }
                try {
                    CloneCatalog.stage(context, record)
                    val output = File(report.outputPath)
                    val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", output)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    pendingInstallerPkg = record.clonePackage
                    pendingInstallerFile = output.absolutePath
                    installer.launch(intent)
                    refresh++
                } catch (e: Exception) {
                    CloneCatalog.dropIfAbsent(context, record.clonePackage)
                    toast(context, "Falha ao abrir instalador: " + (e.message ?: "desconhecida"))
                    refresh++
                }
            }
            ApkJobStage.ERROR, ApkJobStage.CANCELLED -> {
                pendingJobId = 0L
                pendingJobEntry = null
                toast(context, "Falha na clonagem: " + (jobState.error ?: "cancelada"))
            }
            else -> Unit
        }
    }

    if (!permissionsDone || !canInstall) {
        PermissionOnboarding(canInstall) {
            onboarding.edit().putBoolean("onboarded", true).apply()
            permissionsDone = true
            refresh++
        }
        return
    }

    val installed = clones.filter { it.status == CloneEntry.INSTALLED }
    val occupied = clones.size
    val grouped = installed.groupBy { it.sourcePackage }.values.sortedBy { it.first().sourceLabel.lowercase() }

    fun openPicker() {
        if (!CloneRules.canCreate(occupied, pro)) {
            toast(context, "Limite Free: 2 clones. Desinstale um ou selecione o modo Pro de teste.")
            return
        }
        if (!canInstallPackages(context)) {
            installSettings(context)
            return
        }
        scope.launch {
            loading = "Procurando aplicativos…"
            pickerApps = withContext(Dispatchers.IO) { CloneCatalog.userApplications(context) }
            loading = null
            picker = true
        }
    }

    fun openSource(source: CloneSource) {
        picker = false
        scope.launch {
            loading = "A preparar " + source.label + "…"
            val target = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    val info = context.packageManager.getApplicationInfo(source.packageName, 0)
                    val base = File(info.sourceDir)
                    val splits = info.splitSourceDirs?.map(::File)?.filter { it.isFile } ?: emptyList()
                    val meta = ApkRewriter.inspect(base, splits)
                    EditorTarget(source, meta, base, splits)
                }
            }
            loading = null
            target.onSuccess { editor = it }
                .onFailure { toast(context, "Não foi possível ler o APK: " + (it.message ?: "erro desconhecido")) }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("Clone Hub", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            (if (pro) "Pro (teste)" else "Free") + " · " + installed.size +
                " instalados" + if (!pro) " / " + CloneRules.FREE_LIMIT else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (BuildConfig.DEBUG) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Pro para testes", modifier = Modifier.weight(1f))
                Switch(checked = pro, onCheckedChange = {
                    ClonePlan.setQaPro(context, it)
                    pro = ClonePlan.isPro(context)
                })
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { openPicker() }, modifier = Modifier.fillMaxWidth(), enabled = loading == null && pendingJobId == 0L) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Clonar aplicativo")
        }
        if (loading != null) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(loading ?: "", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
        if (selected.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(selected.size.toString() + " selecionados", modifier = Modifier.weight(1f))
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Opções") }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Selecionar todos") }, onClick = {
                        selected = installed.map { it.clonePackage }.toSet()
                        expanded = false
                    })
                    DropdownMenuItem(text = { Text("Limpar seleção") }, onClick = { selected = emptySet(); expanded = false })
                }
                IconButton(onClick = { uninstallConfirmation = selected.toList() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Desinstalar selecionados")
                }
            }
        }
        if (installed.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(58.dp))
                Text("Ainda não há clones instalados.", style = MaterialTheme.typography.titleMedium)
                Text("Use Clonar aplicativo para criar a primeira cópia.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(grouped, key = { it.first().sourcePackage }) { group ->
                    val source = group.first()
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = {
                                if (selected.isNotEmpty()) {
                                    val ids = group.map { it.clonePackage }.toSet()
                                    selected = if (ids.all { it in selected }) selected - ids else selected + ids
                                } else activeGroup = source.sourcePackage
                            },
                            onLongClick = {
                                selected = selected + group.map { it.clonePackage }.toSet()
                            },
                        ),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 3.dp,
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            CloneIcon(source.sourcePackage)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(source.sourceLabel, fontWeight = FontWeight.SemiBold)
                                Text(
                                    group.size.toString() + (if (group.size == 1) " clone" else " clones") +
                                        " · tocar para gerir",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                if (group.size == 1) Text(group.first().cloneLabel, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("›", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
        HorizontalDivider()
        TextButton(
            onClick = { removeHostWithAll = true },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Desinstalar todos os clones e o Clone Hub") }
        Text(
            "A desinstalação direta do Clone Hub pelas Definições do Android NÃO desinstala os clones. Use o fluxo acima e confirme cada remoção.",
            style = MaterialTheme.typography.bodySmall,
        )
    }

    if (picker) AppPickerDialog(sources = pickerApps, onDismiss = { picker = false }, onPick = ::openSource)

    val editing = editor
    if (editing != null) {
        val suggested = remember(editing.source.packageName) {
            CloneCatalog.nextClonePackage(context, editing.source.packageName)
        }
        ApkEditorDialog(
            meta = editing.meta,
            kind = ApkJobKind.CLONE,
            defaultOutputName = editing.source.packageName.substringAfterLast('.') + "-" + System.currentTimeMillis() + ".apk",
            initialPackageName = suggested,
            requireInstall = true,
            onDismiss = { editor = null },
            onManageKeys = { showKeys = true },
            onConfirm = { request ->
                val desiredPackage = request.edits.packageName ?: editing.source.packageName
                val occupiedNow = CloneCatalog.occupiedSlots(context)
                when {
                    !CloneRules.canCreate(occupiedNow, ClonePlan.isPro(context)) ->
                        toast(context, "Limite de clones atingido.")
                    desiredPackage == editing.source.packageName || PackageNames.problem(desiredPackage) != null ->
                        toast(context, "É necessário escolher um identificador novo e válido.")
                    CloneCatalog.isInstalled(context, desiredPackage) ||
                        clones.any { it.clonePackage == desiredPackage } ->
                        toast(context, "Este clone já está instalado.")
                    ApkJobService.busyReason() != null ->
                        toast(context, "Outra operação APK ainda está em execução.")
                    else -> {
                        val outDir = File(context.filesDir, "clone-apks").apply { mkdirs() }
                        val safeName = request.outputName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                        val out = File(outDir, System.currentTimeMillis().toString() + "-" + safeName)
                        val entry = CloneEntry(
                            sourcePackage = editing.source.packageName,
                            sourceLabel = editing.source.label,
                            clonePackage = desiredPackage,
                            cloneLabel = request.edits.label ?: editing.meta.label,
                            launcherClass = editing.source.launcherClass,
                        )
                        val edits = request.edits.copy(hideLauncher = true)
                        runCatching {
                            val req = ApkJobRequest(
                                kind = ApkJobKind.CLONE,
                                input = editing.base.absolutePath,
                                splits = editing.splits.map { it.absolutePath },
                                output = out.absolutePath,
                                edits = edits,
                                key = request.key,
                                schemes = request.schemes,
                                installAfter = true,
                            )
                            pendingJobId = ApkJobService.start(context, req)
                            pendingJobEntry = entry
                            editor = null
                        }.onFailure { toast(context, "Falha ao iniciar clonagem: " + (it.message ?: "")) }
                    }
                }
            },
        )
    }

    if (showKeys) KeyManagerDialog(onDismiss = { showKeys = false })

    val modalGroup = installed.filter { it.sourcePackage == activeGroup }
    if (activeGroup != null && modalGroup.isNotEmpty()) {
        GroupModal(
            sourceLabel = modalGroup.first().sourceLabel,
            entries = modalGroup,
            selection = selected,
            pro = pro,
            onDismiss = { activeGroup = null },
            onToggle = { pkg ->
                selected = if (pkg in selected) selected - pkg else selected + pkg
            },
            onOpen = { entry ->
                if (!CloneCatalog.launch(context, entry)) toast(context, "O Android não conseguiu abrir o clone.")
            },
            onInfo = { entry -> CloneCatalog.showAndroidAppInfo(context, entry.clonePackage) },
            onUninstall = { entry -> uninstallConfirmation = listOf(entry.clonePackage) },
            onShortcut = { entry ->
                val requested = CloneShortcuts.pin(context, entry)
                toast(context, if (requested) "Pedido de atalho enviado ao ecrã inicial." else "Este launcher não permite adicionar o atalho.")
            },
        )
    }

    if (uninstallConfirmation != null) {
        val targets = uninstallConfirmation.orEmpty()
        AlertDialog(
            onDismissRequest = { uninstallConfirmation = null },
            title = { Text("Desinstalar " + targets.size + " clone(s)?") },
            text = { Text("O Android solicitará confirmação para cada clone. Os dados de cada instalação serão eliminados. O aplicativo original não será afetado.") },
            confirmButton = {
                TextButton(onClick = {
                    uninstallQueue = targets.distinct()
                    uninstallConfirmation = null
                    selected = emptySet()
                    activeGroup = null
                }) { Text("Desinstalar") }
            },
            dismissButton = { TextButton(onClick = { uninstallConfirmation = null }) { Text("Cancelar") } },
        )
    }

    if (removeHostWithAll) {
        AlertDialog(
            onDismissRequest = { removeHostWithAll = false },
            title = { Text("Remover clones e Clone Hub?") },
            text = {
                Text(
                    "Os clones são instalações Android separadas. Primeiro terá de confirmar a remoção de cada um. " +
                        "Somente se todos forem removidos será aberta a desinstalação do Clone Hub. " +
                        "Se remover o Clone Hub diretamente pelas Definições, os clones continuarão instalados.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    removeHostWithAll = false
                    awaitingHostRemoval = true
                    uninstallQueue = CloneCatalog.installed(context).map { it.clonePackage }
                    refresh++
                }) { Text("Iniciar remoção completa") }
            },
            dismissButton = { TextButton(onClick = { removeHostWithAll = false }) { Text("Cancelar") } },
        )
    }
}
