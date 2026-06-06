<?php
include_once __DIR__ . '/../dao/IDao.php';
include_once __DIR__ . '/../classe/Position.php';
include_once __DIR__ . '/../connexion/Connexion.php';

class PositionService implements IDao {
    private $connexion;

    public function __construct() {
        $this->connexion = new Connexion();
    }

    public function create($position): bool {
        $sql  = "INSERT INTO position (latitude, longitude, date_position, imei)
                 VALUES (:latitude, :longitude, :date_position, :imei)";
        $stmt = $this->connexion->getConnexion()->prepare($sql);
        return $stmt->execute([
            ':latitude'      => $position->getLatitude(),
            ':longitude'     => $position->getLongitude(),
            ':date_position' => $position->getDatePosition(),
            ':imei'          => $position->getImei(),
        ]);
    }

    public function getAll(): array {
        $stmt = $this->connexion->getConnexion()->query(
            "SELECT * FROM position ORDER BY date_position DESC"
        );
        return $stmt->fetchAll();
    }

    public function getById($id): ?array {
        $stmt = $this->connexion->getConnexion()->prepare(
            "SELECT * FROM position WHERE id = :id"
        );
        $stmt->execute([':id' => $id]);
        $r = $stmt->fetch();
        return $r ?: null;
    }

    public function update($obj): void {}
    public function delete($obj): void {}
}
?>
