<?php
header("Content-Type: application/json; charset=utf-8");
header("Access-Control-Allow-Origin: *");

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo json_encode(['status' => 'error', 'message' => 'Méthode non autorisée.']);
    exit;
}

include_once __DIR__ . '/service/PositionService.php';

// Lecture directe $_POST + conversion de type explicite
$latitude     = isset($_POST['latitude'])      ? floatval($_POST['latitude'])     : null;
$longitude    = isset($_POST['longitude'])     ? floatval($_POST['longitude'])    : null;
$datePosition = isset($_POST['date_position']) ? trim($_POST['date_position'])    : '';
$imei         = isset($_POST['imei'])          ? trim($_POST['imei'])             : 'unknown';

// Validation
if ($latitude === null || $longitude === null || empty($datePosition)) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Paramètres invalides.']);
    exit;
}

$service  = new PositionService();
$position = new Position(null, $latitude, $longitude, $datePosition, $imei);
$ok       = $service->create($position);

if ($ok) {
    http_response_code(201);
    echo json_encode(['status' => 'success', 'message' => 'Position enregistrée.']);
} else {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Erreur insertion BDD.']);
}
?>
