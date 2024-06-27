package com.apispringboot.productinventory.controllers;

import com.apispringboot.productinventory.dto.ProductDTO;
import com.apispringboot.productinventory.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import javax.validation.ValidationException;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;


@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Create a new product", description = "Creates a new product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created product!", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request."),
            @ApiResponse(responseCode = "422", description = "Invalid fields or data inconsistencies."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/addProduct")
    public ResponseEntity<EntityModel<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
    try {
        ProductDTO createdProduct = productService.create(productDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.getId())
                .toUri();
        return ResponseEntity.created(location).body(addHateoasLinks(createdProduct));
    } catch (ValidationException ex) {
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid fields or data inconsistencies.", ex);
    } catch (DataIntegrityViolationException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request.", ex);
    } catch (Exception ex) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.", ex);
    }
}

    
    @Operation(summary = "Get all products", description = "Get all registered products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found!", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class))) }),
            @ApiResponse(responseCode = "404", description = "No products found.")
    })
    @GetMapping("/getAllProducts")
    public ResponseEntity<List<EntityModel<ProductDTO>>> getAllProducts() {
        List<ProductDTO> products = productService.findAll();
        if (!products.isEmpty()) {
            List<EntityModel<ProductDTO>> productsWithLinks = products.stream()
                    .map(this::addHateoasLinks)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(productsWithLinks);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Get product by ID", description = "Gets a product by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found!", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "No product found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("getProduct/{id}")
    public ResponseEntity<EntityModel<ProductDTO>> getProductById(@PathVariable Long id) {
        try {
            ProductDTO productDTO = productService.findById(id);
            if (productDTO != null) {
                return ResponseEntity.ok(addHateoasLinks(productDTO));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.", ex);
        }
    }


    @Operation(summary = "Delete a product", description = "Delete a product by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully!"),
            @ApiResponse(responseCode = "404", description = "Product not found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @DeleteMapping("deleteProduct/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            boolean deleted = productService.delete(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.", ex);
        }
    }


    @Operation(summary = "Update product", description = "Update all product information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully!", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid JSON."),
            @ApiResponse(responseCode = "404", description = "Product not found."),
            @ApiResponse(responseCode = "422", description = "Invalid fields or data inconsistencies."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PutMapping("editProduct/{id}")
    public ResponseEntity<EntityModel<ProductDTO>> replaceProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        try {
            productDTO.setId(id);
            boolean updated = productService.update(productDTO);
            if (updated) {
                return ResponseEntity.ok(addHateoasLinks(productDTO));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON", ex);
            }
        }
    }


    @Operation(summary = "Partially update a product", description = "Partially update a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully!", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid JSON."),
            @ApiResponse(responseCode = "404", description = "Product not found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PatchMapping("editProduct/{id}")
    public ResponseEntity<EntityModel<ProductDTO>> updateProductPartial(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
    ProductDTO existingProduct = productService.findById(id);
        if (existingProduct != null) {
            if (productDTO.getName() != null) {
                existingProduct.setName(productDTO.getName());
            }
            if (productDTO.getDescription() != null) {
                existingProduct.setDescription(productDTO.getDescription());
            }
            if (productDTO.getPrice() != null) {
                existingProduct.setPrice(productDTO.getPrice());
            }
            if (productDTO.getQuantity() != null) {
                existingProduct.setQuantity(productDTO.getQuantity());
            }
            if (productDTO.getCategory() != null) {
                existingProduct.setCategory(productDTO.getCategory());
            }

            boolean updated = productService.update(existingProduct);
            if (updated) {
                return ResponseEntity.ok(addHateoasLinks(existingProduct));
            }
        }
        return ResponseEntity.notFound().build();
}


    private EntityModel<ProductDTO> addHateoasLinks(ProductDTO productDTO) {
        return EntityModel.of(productDTO,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class).getProductById(productDTO.getId())).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class).getAllProducts()).withRel("all-products"));
    }
}
