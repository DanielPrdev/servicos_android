package com.example.servicos;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ServicoAdapter.OnServicoActionListener {

    private RecyclerView rvServicos;
    private FloatingActionButton fabAdd;
    private ServicoAdapter servicoAdapter;
    private BancoDados bancoDados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bancoDados = new BancoDados(this);

        rvServicos = findViewById(R.id.rvTasks);
        fabAdd = findViewById(R.id.fabAdd);

        rvServicos.setLayoutManager(new LinearLayoutManager(this));
        servicoAdapter = new ServicoAdapter(this);
        rvServicos.setAdapter(servicoAdapter);

        fabAdd.setOnClickListener(v -> showAddOrEditServicoDialog(null));

        loadServicos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServicos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareServicos();
            return true;
        } else if (item.getItemId() == R.id.action_trash) {
            showTrashOptionsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTrashOptionsDialog() {
        String[] options = {"Excluir serviços passados", "Excluir todos os serviços"};
        new AlertDialog.Builder(this)
                .setTitle("Gerenciar Lixeira")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        confirmDeletePastServicos();
                    } else if (which == 1) {
                        confirmDeleteAllServicos();
                    }
                })
                .show();
    }

    private void confirmDeletePastServicos() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Passados")
                .setMessage("Deseja realmente excluir todos os serviços cuja data/horário já passaram?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    bancoDados.excluirPassados();
                    loadServicos();
                    Toast.makeText(this, "Serviços passados excluídos", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmDeleteAllServicos() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Todos")
                .setMessage("Deseja realmente excluir TODOS os serviços cadastrados?")
                .setPositiveButton("Excluir Todos", (dialog, which) -> {
                    bancoDados.excluirTodos();
                    loadServicos();
                    Toast.makeText(this, "Todos os serviços foram excluídos", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void shareServicos() {
        List<Servico> servicos = bancoDados.listar();
        if (servicos.isEmpty()) {
            Toast.makeText(this, "Nenhum serviço para compartilhar", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Meus Serviços:\n\n");
        for (Servico s : servicos) {
            sb.append("- ").append(s.getTitle());
            if (!TextUtils.isEmpty(s.getClientName())) {
                sb.append(" (Cliente: ").append(s.getClientName()).append(")");
            }
            sb.append("\n  Valor: ").append(s.getFormattedPrice())
              .append(" | Data: ").append(s.getDate());
            if (!TextUtils.isEmpty(s.getTime())) {
                sb.append(" às ").append(s.getTime());
            }
            sb.append("\n");
            if (!TextUtils.isEmpty(s.getAddress())) {
                sb.append("  Endereço: ").append(s.getAddress()).append("\n");
            }
            if (!TextUtils.isEmpty(s.getDescription())) {
                sb.append("  Obs: ").append(s.getDescription()).append("\n");
            }
            sb.append("\n");
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Lista de Serviços");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString().trim());
        startActivity(Intent.createChooser(intent, "Compartilhar via"));
    }

    private void loadServicos() {
        List<Servico> servicos = bancoDados.listar();
        servicoAdapter.setServicos(servicos);
        for (Servico s : servicos) {
            AlarmScheduler.scheduleAlarmForServico(this, s);
        }
    }

    @Override
    public void onServicoClick(Servico servico) {
        showServicoDetailDialog(servico);
    }

    @Override
    public void onEditClick(Servico servico) {
        showAddOrEditServicoDialog(servico);
    }

    @Override
    public void onDeleteClick(Servico servico) {
        confirmDeleteServico(servico);
    }

    private void confirmDeleteServico(Servico servico) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Serviço")
                .setMessage("Deseja realmente excluir o serviço \"" + servico.getTitle() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    bancoDados.excluir(servico.getId());
                    loadServicos();
                    Toast.makeText(this, "Serviço excluído", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showServicoDetailDialog(Servico servico) {
        StringBuilder sb = new StringBuilder();
        sb.append("Título: ").append(servico.getTitle()).append("\n\n");
        if (!TextUtils.isEmpty(servico.getClientName())) {
            sb.append("Cliente: ").append(servico.getClientName()).append("\n\n");
        }
        sb.append("Valor: ").append(servico.getFormattedPrice()).append("\n\n");
        sb.append("Data: ").append(servico.getDate());
        if (!TextUtils.isEmpty(servico.getTime())) {
            sb.append(" às ").append(servico.getTime());
        }
        sb.append("\n\n");
        if (!TextUtils.isEmpty(servico.getAddress())) {
            sb.append("Endereço: ").append(servico.getAddress()).append("\n\n");
        }
        if (!TextUtils.isEmpty(servico.getDescription())) {
            sb.append("Descrição:\n").append(servico.getDescription());
        }

        new AlertDialog.Builder(this)
                .setTitle("Detalhes do Serviço")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Fechar", null)
                .setNeutralButton("Editar", (dialog, which) -> showAddOrEditServicoDialog(servico))
                .show();
    }

    private void showAddOrEditServicoDialog(final Servico servicoToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        final TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        final TextInputLayout tilClientName = dialogView.findViewById(R.id.tilTaskClientName);
        final TextInputLayout tilTitle = dialogView.findViewById(R.id.tilTaskTitle);
        final EditText etClientName = dialogView.findViewById(R.id.etTaskClientName);
        final EditText etTitle = dialogView.findViewById(R.id.etTaskTitle);
        final EditText etPrice = dialogView.findViewById(R.id.etTaskPrice);
        final EditText etAddress = dialogView.findViewById(R.id.etTaskAddress);
        final EditText etDate = dialogView.findViewById(R.id.etTaskDate);
        final EditText etTime = dialogView.findViewById(R.id.etTaskTime);
        final EditText etDesc = dialogView.findViewById(R.id.etTaskDescription);

        final Calendar calendar = Calendar.getInstance();

        etDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));
                        etDate.setText(sdf.format(calendar.getTime()));
                    }, year, month, day);
            datePickerDialog.show();
        });

        etTime.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        etTime.setText(timeStr);
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        boolean isEdit = servicoToEdit != null;

        if (isEdit) {
            tvDialogTitle.setText("Editar Serviço");
            etClientName.setText(servicoToEdit.getClientName());
            etTitle.setText(servicoToEdit.getTitle());
            if (servicoToEdit.getPrice() > 0) {
                etPrice.setText(String.format(Locale.US, "%.2f", servicoToEdit.getPrice()));
            }
            etAddress.setText(servicoToEdit.getAddress());
            etDate.setText(servicoToEdit.getDate());
            etTime.setText(servicoToEdit.getTime());
            etDesc.setText(servicoToEdit.getDescription());
        } else {
            tvDialogTitle.setText("Novo Serviço");
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));
            etDate.setText(sdfDate.format(new Date()));

            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
            etTime.setText(sdfTime.format(new Date()));
        }

        builder.setPositiveButton(isEdit ? "Atualizar" : "Salvar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String clientName = etClientName.getText() != null ? etClientName.getText().toString().trim() : "";
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim().replace(",", ".") : "";
            String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
            String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
            String time = etTime.getText() != null ? etTime.getText().toString().trim() : "";
            String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";

            boolean hasError = false;

            if (TextUtils.isEmpty(clientName)) {
                tilClientName.setError("Nome do cliente é obrigatório");
                hasError = true;
            } else {
                tilClientName.setError(null);
            }

            if (TextUtils.isEmpty(title)) {
                tilTitle.setError("Título é obrigatório");
                hasError = true;
            } else {
                tilTitle.setError(null);
            }

            if (hasError) {
                return;
            }

            double price = 0;
            try {
                if (!TextUtils.isEmpty(priceStr)) {
                    price = Double.parseDouble(priceStr);
                }
            } catch (NumberFormatException ignored) {}

            if (isEdit) {
                servicoToEdit.setClientName(clientName);
                servicoToEdit.setTitle(title);
                servicoToEdit.setPrice(price);
                servicoToEdit.setAddress(address);
                servicoToEdit.setDate(date);
                servicoToEdit.setTime(time);
                servicoToEdit.setDescription(desc);
                bancoDados.atualizar(servicoToEdit);
                AlarmScheduler.scheduleAlarmForServico(MainActivity.this, servicoToEdit);
                Toast.makeText(MainActivity.this, "Serviço atualizado", Toast.LENGTH_SHORT).show();
            } else {
                Servico novo = new Servico(0, title, desc, date, time, clientName, address, price);
                long newId = bancoDados.inserir(novo);
                novo.setId(newId);
                AlarmScheduler.scheduleAlarmForServico(MainActivity.this, novo);
                Toast.makeText(MainActivity.this, "Serviço salvo", Toast.LENGTH_SHORT).show();
            }

            loadServicos();
            dialog.dismiss();
        });
    }
}
