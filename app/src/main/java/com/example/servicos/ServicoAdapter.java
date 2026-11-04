package com.example.servicos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ServicoAdapter extends RecyclerView.Adapter<ServicoAdapter.ServicoViewHolder> {

    public interface OnServicoActionListener {
        void onServicoClick(Servico servico);
        void onEditClick(Servico servico);
        void onDeleteClick(Servico servico);
    }

    private List<Servico> servicos = new ArrayList<>();
    private final OnServicoActionListener listener;

    public ServicoAdapter(OnServicoActionListener listener) {
        this.listener = listener;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos != null ? servicos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServicoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new ServicoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicoViewHolder holder, int position) {
        Servico servico = servicos.get(position);
        holder.bind(servico, listener);
    }

    @Override
    public int getItemCount() {
        return servicos.size();
    }

    static class ServicoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvClientName;
        private final TextView tvPrice;
        private final TextView tvDate;
        private final TextView btnMenu;

        public ServicoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvClientName = itemView.findViewById(R.id.tvItemClientName);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }

        public void bind(final Servico servico, final OnServicoActionListener listener) {
            tvTitle.setText(servico.getTitle());

            String client = servico.getClientName();
            if (client != null && !client.trim().isEmpty()) {
                tvClientName.setText("Cliente: " + client);
                tvClientName.setVisibility(View.VISIBLE);
            } else {
                tvClientName.setVisibility(View.GONE);
            }

            tvPrice.setText(servico.getFormattedPrice());

            String dateTimeStr = servico.getDate();
            if (servico.getTime() != null && !servico.getTime().trim().isEmpty()) {
                dateTimeStr += " às " + servico.getTime();
            }
            tvDate.setText(dateTimeStr);

            // Check if date has passed -> make date reddish
            boolean isPast = false;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));
                sdf.setLenient(false);
                Date serviceDate = sdf.parse(servico.getDate());
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                Calendar servCal = Calendar.getInstance();
                if (serviceDate != null) {
                    servCal.setTime(serviceDate);
                    servCal.set(Calendar.HOUR_OF_DAY, 0);
                    servCal.set(Calendar.MINUTE, 0);
                    servCal.set(Calendar.SECOND, 0);
                    servCal.set(Calendar.MILLISECOND, 0);

                    if (servCal.before(today)) {
                        isPast = true;
                    }
                }
            } catch (Exception ignored) {}

            if (isPast) {
                tvDate.setTextColor(Color.parseColor("#D32F2F"));
            } else {
                tvDate.setTextColor(Color.parseColor("#757575"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServicoClick(servico);
                }
            });

            btnMenu.setOnClickListener(v -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), v);
                popup.getMenu().add("Editar");
                popup.getMenu().add("Excluir");
                popup.setOnMenuItemClickListener(item -> {
                    CharSequence title = item.getTitle();
                    if (title != null) {
                        String titleStr = title.toString();
                        if ("Editar".equals(titleStr)) {
                            if (listener != null) listener.onEditClick(servico);
                            return true;
                        } else if ("Excluir".equals(titleStr)) {
                            if (listener != null) listener.onDeleteClick(servico);
                            return true;
                        }
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}
