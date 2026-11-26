package br.com.uniesp.centralencomendas.cliente.repository;

import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
