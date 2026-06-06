<?php
class Connexion {
    private $connexion;

    public function __construct() {
        $host     = 'localhost';
        $dbname   = 'localisation';
        $login    = 'root';
        $password = '';
        try {
            $this->connexion = new PDO(
                "mysql:host=$host;dbname=$dbname;charset=utf8mb4",
                $login, $password
            );
            $this->connexion->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $this->connexion->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
        } catch (PDOException $e) {
            http_response_code(500);
            die(json_encode(['status' => 'error', 'message' => 'Erreur de connexion BDD.']));
        }
    }

    public function getConnexion(): PDO { return $this->connexion; }
}
?>
