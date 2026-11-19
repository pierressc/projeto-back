package br.com.uniesp.centralencomendas.produtocatalogo.repository;

import br.com.uniesp.centralencomendas.produtocatalogo.entity.ProdutoCatalogo;
import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoCatalogoRepository extends JpaRepository<ProdutoCatalogo, Long> {

    List<ProdutoCatalogo> findByAtivoTrue();

    List<ProdutoCatalogo> findByCategoria(CategoriaProduto categoria);
}
