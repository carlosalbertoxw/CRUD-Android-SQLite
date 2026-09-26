package com.carlosalbertoxw.crud_android_sqlite.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carlosalbertoxw.crud_android_sqlite.R
import com.carlosalbertoxw.crud_android_sqlite.ui.FormEvent
import com.carlosalbertoxw.crud_android_sqlite.ui.NoteFormViewModel
import kotlinx.coroutines.launch

/**
 * Formulario para crear o editar una nota.
 *
 * @param onDone se llama al terminar (guardado, cancelado o nota inexistente).
 * @param onDelete se llama al pulsar "Eliminar"; el borrado (y su "Deshacer")
 * lo gestiona la lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteFormScreen(
    viewModel: NoteFormViewModel,
    onDone: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val text by viewModel.text.collectAsStateWithLifecycle()
    val hasChanges by viewModel.hasChanges.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val emptyMessage = stringResource(R.string.validation_empty)
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val currentOnDone by rememberUpdatedState(onDone)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                FormEvent.Saved, FormEvent.NotFound -> currentOnDone()
                FormEvent.ValidationError ->
                    scope.launch { snackbarHostState.showSnackbar(emptyMessage) }
            }
        }
    }

    // Salir con cambios sin guardar (botón atrás del sistema o de la barra)
    // pide confirmación en lugar de perderlos en silencio.
    val requestExit: () -> Unit = {
        if (hasChanges) showDiscardDialog = true else onDone()
    }
    BackHandler(enabled = hasChanges) { showDiscardDialog = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (viewModel.isEditing) R.string.edit_note else R.string.new_note
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = requestExit) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (viewModel.isEditing) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::save) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Incluir el teclado (IME) en los insets hace que el contenido y el
        // botón de guardar suban cuando aparece, en vez de quedar tapados.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.field_title)) }
            )
            OutlinedTextField(
                value = text,
                onValueChange = viewModel::onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                minLines = 6,
                label = { Text(stringResource(R.string.field_text)) }
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDone()
                }) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.keep_editing))
                }
            }
        )
    }
}
