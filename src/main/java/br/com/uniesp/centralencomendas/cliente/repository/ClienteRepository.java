package br.com.uniesp.centralencomendas.cliente.repository;

import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNomeContainingIgnoreCase(String nome);
}
