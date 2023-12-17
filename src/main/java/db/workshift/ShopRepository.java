package db.workshift;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ShopRepository extends CrudRepository<Shop, UUID> {

}
