package com.example.healthplanner.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.healthplanner.R;
import com.example.healthplanner.database.DatabaseHelper;
import com.example.healthplanner.models.Meal;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class MealIntervalWorker extends Worker {

    private static final String CHANNEL_ID = "meal_interval_channel";

    public MealIntervalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        Meal lastMeal = dbHelper.getLastMeal();

        if (lastMeal != null) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(lastMeal.getMealtime(), now);

            if (duration.toMinutes() >= 1) {
                sendNotification();
            }
        }

        // Agendar a próxima execução para daqui a 1 minuto (gambiarra para teste)
        OneTimeWorkRequest nextRequest = new OneTimeWorkRequest.Builder(MealIntervalWorker.class)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .addTag("MealIntervalTest")
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(nextRequest);

        return Result.success();
    }

    private void sendNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lembrete de Refeição",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Usando ícone padrão por enquanto
                .setContentTitle("Hora de Comer? (Teste 1 min)")
                .setContentText("Faz mais de um minuto desde a sua última refeição.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
