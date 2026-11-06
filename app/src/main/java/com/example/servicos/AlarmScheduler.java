package com.example.servicos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmScheduler {

    public static void scheduleAlarmForServico(Context context, Servico servico) {
        try {
            String dateStr = servico.getDate();
            String timeStr = servico.getTime();
            if (dateStr == null || timeStr == null || timeStr.trim().isEmpty()) return;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"));
            sdf.setLenient(false);
            Date serviceDate = sdf.parse(dateStr.trim() + " " + timeStr.trim());
            if (serviceDate == null) return;

            // 30 minutes before
            long triggerTime = serviceDate.getTime() - (30 * 60 * 1000L);
            long now = System.currentTimeMillis();

            if (triggerTime <= now) {
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ServicoAlarmReceiver.class);
            intent.putExtra("title", servico.getTitle());
            intent.putExtra("client", servico.getClientName());
            intent.putExtra("time", timeStr);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) servico.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

        } catch (Exception ignored) {}
    }
}
