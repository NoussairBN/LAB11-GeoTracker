<?php
interface IDao {
    public function create($obj);
    public function update($obj);
    public function delete($obj);
    public function getById($id);
    public function getAll();
}
?>
