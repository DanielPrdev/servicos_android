package com.example.servicos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView rvTasks;
    private FloatingActionButton fabAdd;
    private TaskAdapter taskAdapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        rvTasks = findViewById(R.id.rvTasks);
        fabAdd = findViewById(R.id.fabAdd);

        rvTasks.setLayoutManager(new GridLayoutManager(this, 2));
        taskAdapter = new TaskAdapter(this);
        rvTasks.setAdapter(taskAdapter);

        fabAdd.setOnClickListener(v -> showAddOrEditTaskDialog(null));

        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        List<Task> tasks = dbHelper.getAllTasks();
        taskAdapter.setTasks(tasks);
    }

    private void showAddOrEditTaskDialog(final Task taskToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        final EditText etTitle = dialogView.findViewById(R.id.etTaskTitle);
        final EditText etDesc = dialogView.findViewById(R.id.etTaskDescription);
        final EditText etDate = dialogView.findViewById(R.id.etTaskDate);

        boolean isEdit = taskToEdit != null;

        if (isEdit) {
            etTitle.setText(taskToEdit.getTitle());
            etDesc.setText(taskToEdit.getDescription());
            etDate.setText(taskToEdit.getDate());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
            etDate.setText(sdf.format(new Date()));
        }

        builder.setPositiveButton(isEdit ? "Atualizar" : "Salvar", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(MainActivity.this, "Título é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit) {
                taskToEdit.setTitle(title);
                taskToEdit.setDescription(desc);
                taskToEdit.setDate(date);
                dbHelper.updateTask(taskToEdit);
                Toast.makeText(MainActivity.this, "Serviço atualizado!", Toast.LENGTH_SHORT).show();
            } else {
                Task newTask = new Task(title, desc, date, "");
                dbHelper.addTask(newTask);
                Toast.makeText(MainActivity.this, "Serviço adicionado!", Toast.LENGTH_SHORT).show();
            }

            loadTasks();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onTaskClick(Task task) {
        showTaskOptionsDialog(task);
    }

    @Override
    public void onTaskLongClick(Task task) {
        showTaskOptionsDialog(task);
    }

    private void showTaskOptionsDialog(final Task task) {
        String[] options = {"Editar", "Excluir"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(task.getTitle());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showAddOrEditTaskDialog(task);
            } else if (which == 1) {
                confirmDeleteTask(task);
            }
        });
        builder.show();
    }

    private void confirmDeleteTask(final Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Serviço")
                .setMessage("Deseja realmente excluir '" + task.getTitle() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    dbHelper.deleteTask(task.getId());
                    loadTasks();
                    Toast.makeText(MainActivity.this, "Serviço excluído", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
