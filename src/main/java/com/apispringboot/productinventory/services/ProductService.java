package com.apispringboot.productinventory.services;

import com.apispringboot.productinventory.dto.ProductDTO;
import com.apispringboot.productinventory.mapper.ProductMapper;
import com.apispringboot.productinventory.models.Product;
import com.apispringboot.productinventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements IService<ProductDTO> {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ProductDTO create(ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    @Override
    public ProductDTO findById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(productMapper::toDTO).orElse(null);
    }

    @Override
    public List<ProductDTO> findAll() {
        List<Product> products = productRepository.findAll();
        return productMapper.toDTO(products);
    }

    @Override
    public boolean update(ProductDTO productDTO) {
        Optional<Product> existingProductOpt = productRepository.findById(productDTO.getId());
        if (existingProductOpt.isPresent()) {
            Product existingProduct = existingProductOpt.get();
            existingProduct.setName(productDTO.getName());
            existingProduct.setDescription(productDTO.getDescription());
            existingProduct.setPrice(productDTO.getPrice());
            existingProduct.setQuantity(productDTO.getQuantity());
            existingProduct.setCategory(productDTO.getCategory());
            productRepository.save(existingProduct);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public boolean partialUpdate(ProductDTO productDTO) {
        throw new UnsupportedOperationException("Unimplemented method 'partialUpdate'");
    }
}
