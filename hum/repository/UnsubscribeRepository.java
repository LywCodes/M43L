package ita.repository;

import ita.entity.Unsubscribe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnsubscribeRepository extends JpaRepository<Unsubscribe, UUID> {

}
