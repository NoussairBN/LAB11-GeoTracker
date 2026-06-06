package com.example.localisationsmartphone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * =========================================================
 *  GeoTracker — TP 11 : Localisation d'un smartphone
 *  Thème : System Dashboard (#121212 / #00E5FF)
 * =========================================================
 *  Fonctionnalités :
 *  - Récupération de la position GPS via LocationManager
 *  - Affichage des coordonnées dans l'interface
 *  - Envoi HTTP POST vers un serveur PHP via Volley
 *  - Gestion des permissions runtime (ACCESS_FINE_LOCATION)
 * =========================================================
 */
public class MainActivity extends AppCompatActivity {

    // ── CONSTANTE SERVEUR ─────────────────────────────────────
    // ⚠ Remplacer VOTRE_IP par l'adresse IP locale de votre poste XAMPP
    // Exemple : "http://192.168.1.10/localisation/createPosition.php"
    private static final String SERVER_URL =
            "http://192.168.100.246/localisation/createPosition.php";

    // ── VUES ──────────────────────────────────────────────────
    private TextView tvGpsStatus, tvProvider;
    private TextView tvLatitude, tvLongitude, tvAltitude, tvAccuracy;
    private TextView tvDate, tvImei, tvServerStatus;
    private Button   btnSend;

    // ── ÉTAT ──────────────────────────────────────────────────
    private double  latitude   = 0;
    private double  longitude  = 0;
    private double  altitude   = 0;
    private float   accuracy   = 0;
    private String  lastDate   = "—";
    private String  deviceImei = "unknown";
    private boolean hasLocation = false;

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Liaisons avec les vues du layout
        tvGpsStatus    = findViewById(R.id.tvGpsStatus);
        tvProvider     = findViewById(R.id.tvProvider);
        tvLatitude     = findViewById(R.id.tvLatitude);
        tvLongitude    = findViewById(R.id.tvLongitude);
        tvAltitude     = findViewById(R.id.tvAltitude);
        tvAccuracy     = findViewById(R.id.tvAccuracy);
        tvDate         = findViewById(R.id.tvDate);
        tvImei         = findViewById(R.id.tvImei);
        tvServerStatus = findViewById(R.id.tvServerStatus);
        btnSend        = findViewById(R.id.btnSend);

        // Bouton envoi manuel
        btnSend.setOnClickListener(v -> {
            if (hasLocation) {
                sendPositionToServer();
            } else {
                Toast.makeText(this, "⚠ Aucune position GPS disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // Récupérer l'IMEI dès le démarrage (si permission accordée)
        resolveImei();

        // Demander les permissions et démarrer le GPS
        checkAndStartGps();
    }

    // =========================================================================
    // Permissions runtime
    // =========================================================================

    /**
     * Launcher moderne (ActivityResult API) pour la permission de localisation.
     * Remplace l'ancienne méthode onRequestPermissionsResult().
     */
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    grants -> {
                        boolean locationOk =
                                Boolean.TRUE.equals(grants.get(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                                Boolean.TRUE.equals(grants.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                        if (locationOk) {
                            startLocationUpdates();
                        } else {
                            setGpsStatus(false, "Permission refusée");
                            Toast.makeText(this,
                                    "❌ Permission GPS refusée — impossible de localiser",
                                    Toast.LENGTH_LONG).show();
                        }

                        // Tenter de récupérer l'IMEI si READ_PHONE_STATE accordé
                        if (Boolean.TRUE.equals(grants.get(Manifest.permission.READ_PHONE_STATE))) {
                            resolveImei();
                        }
                    }
            );

    /** Vérifie les permissions et démarre le GPS si accordées */
    private void checkAndStartGps() {
        boolean locationGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if (locationGranted) {
            startLocationUpdates();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
            });
        }
    }

    // =========================================================================
    // GPS — LocationManager
    // =========================================================================

    /**
     * Active l'écoute des mises à jour de position GPS.
     *
     * Paramètres de requestLocationUpdates :
     *  - provider  : GPS_PROVIDER = antenne GPS intégrée
     *  - minTime   : 30 000 ms = délai minimum entre deux mises à jour
     *  - minDistance: 50 m = déplacement minimum avant une nouvelle mise à jour
     *  - listener  : objet recevant les notifications de position
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Choisir le meilleur provider disponible
        String provider;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            setGpsStatus(false, "GPS désactivé");
            Toast.makeText(this, "⚠ Activez le GPS dans les paramètres", Toast.LENGTH_LONG).show();
            return;
        }

        tvProvider.setText(provider.toUpperCase());

        locationManager.requestLocationUpdates(
                provider,
                30_000L,   // 30 secondes minimum entre deux updates
                50f,       // 50 mètres minimum de déplacement
                locationListener
        );

        setGpsStatus(null, "Recherche du signal…");
    }

    /**
     * Listener GPS — reçoit les nouvelles positions.
     *
     * onLocationChanged() est appelé à chaque nouvelle position détectée.
     * Les autres méthodes gèrent les changements de statut du provider.
     */
    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            // Stocker les coordonnées
            latitude  = location.getLatitude();
            longitude = location.getLongitude();
            altitude  = location.getAltitude();
            accuracy  = location.getAccuracy();
            lastDate  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(new Date());
            hasLocation = true;

            // Mettre à jour l'interface
            runOnUiThread(() -> {
                setGpsStatus(true, "Signal actif");
                tvLatitude.setText(String.format(Locale.getDefault(),  "%.6f °", latitude));
                tvLongitude.setText(String.format(Locale.getDefault(), "%.6f °", longitude));
                tvAltitude.setText(String.format(Locale.getDefault(),  "%.1f m", altitude));
                tvAccuracy.setText(String.format(Locale.getDefault(),  "± %.0f m", accuracy));
                tvDate.setText(lastDate);
            });

            // Envoi automatique à chaque nouvelle position
            sendPositionToServer();
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            runOnUiThread(() -> {
                tvProvider.setText(provider.toUpperCase());
                setGpsStatus(null, "Provider activé : " + provider);
            });
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            runOnUiThread(() -> setGpsStatus(false, "GPS désactivé"));
        }
    };

    // =========================================================================
    // Communication réseau — Volley
    // =========================================================================

    /**
     * Envoie les coordonnées GPS au serveur PHP via une requête HTTP POST.
     *
     * Volley gère automatiquement :
     *  - l'exécution en arrière-plan (hors thread UI)
     *  - la file d'attente des requêtes
     *  - les callbacks de succès et d'erreur sur le thread principal
     *
     * Les paramètres POST envoyés correspondent aux colonnes de la table MySQL :
     *  latitude, longitude, date_position, imei
     */
    private void sendPositionToServer() {
        runOnUiThread(() -> tvServerStatus.setText("⬆ Envoi en cours…"));

        // Capture des valeurs pour le lambda
        final double  lat  = latitude;
        final double  lon  = longitude;
        final String  date = lastDate;
        final String  imei = deviceImei;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                SERVER_URL,
                response -> runOnUiThread(() -> {
                    tvServerStatus.setText("✅ " + response.trim());
                    Toast.makeText(this, "Position envoyée !", Toast.LENGTH_SHORT).show();
                }),
                error -> runOnUiThread(() -> {
                    tvServerStatus.setText("❌ Erreur réseau — vérifier l'IP du serveur");
                    Toast.makeText(this, "Erreur connexion serveur", Toast.LENGTH_SHORT).show();
                })
        ) {
            /**
             * getParams() fournit les paramètres du corps de la requête POST.
             * Ils seront reçus côté PHP via $_POST['latitude'], etc.
             */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("latitude",      String.valueOf(lat));
                params.put("longitude",     String.valueOf(lon));
                params.put("date_position", date);
                params.put("imei",          imei);
                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    // =========================================================================
    // Identifiant terminal (IMEI)
    // =========================================================================

    /**
     * Récupère l'identifiant unique du terminal via TelephonyManager.
     *
     * getDeviceId() est déprécié depuis API 26 mais reste utilisé dans ce TP
     * pour des raisons pédagogiques (illustrer READ_PHONE_STATE).
     * En production : préférer Settings.Secure.getString(ANDROID_ID).
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    private void resolveImei() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                TelephonyManager tm =
                        (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String id = tm.getDeviceId();
                deviceImei = (id != null && !id.isEmpty()) ? id : "unknown";
            } catch (Exception e) {
                deviceImei = "unknown";
            }
        } else {
            deviceImei = "unknown";
        }
        if (tvImei != null) tvImei.setText(deviceImei);
    }

    // =========================================================================
    // Helpers UI
    // =========================================================================

    /**
     * Met à jour le statut GPS affiché (texte + couleur + point).
     *
     * @param active null = en attente, true = actif, false = erreur
     * @param label  Texte à afficher
     */
    private void setGpsStatus(Boolean active, String label) {
        tvGpsStatus.setText(label);
        if (active == null) {
            tvGpsStatus.setTextColor(getColor(R.color.colorWarning));
            findViewById(R.id.dotStatus).setBackgroundResource(R.drawable.bg_dot_waiting);
        } else if (active) {
            tvGpsStatus.setTextColor(getColor(R.color.colorSuccess));
            findViewById(R.id.dotStatus).setBackgroundResource(R.drawable.bg_dot_active);
        } else {
            tvGpsStatus.setTextColor(getColor(R.color.colorError));
            findViewById(R.id.dotStatus).setBackgroundResource(R.drawable.bg_dot_error);
        }
    }
}