package com.upgrad.Grofers.api.controllers;

import com.upgrad.Grofers.api.CategoriesListResponse;
import com.upgrad.Grofers.api.CategoryDetailsResponse;
import com.upgrad.Grofers.api.CategoryListResponse;
import com.upgrad.Grofers.api.ItemList;
import com.upgrad.Grofers.service.business.CategoryService;
import com.upgrad.Grofers.service.entity.CategoryEntity;
import com.upgrad.Grofers.service.entity.ItemEntity;
import com.upgrad.Grofers.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * A controller method to get all address from the database.
     *
     * @param categoryId - The uuid of the category whose detail is asked from the database..
     * @return - ResponseEntity<CategoryDetailsResponse> type object along with Http status OK.
//     * @throws CategoryNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/{category_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoryDetailsResponse> getCategoryById(@PathVariable("category_id") String categoryId) throws CategoryNotFoundException {

        if(categoryId==null)
            throw new CategoryNotFoundException("CNF-001","Category feild should not be empty");

        CategoryEntity categoryEntity= categoryService.getCategoryById(categoryId);
        List<ItemEntity> itemEntities = categoryEntity.getItems();

        List<ItemList> itemLists = new ArrayList<ItemList>();

        for(ItemEntity itemEntity : itemEntities)
        {
            ItemList itemList = new ItemList();
            itemList.setId(UUID.fromString(itemEntity.getUuid()));
            itemList.setItemName(itemEntity.getItemName());
            itemList.setPrice(itemEntity.getPrice());

            itemLists.add(itemList);
        }

        CategoryDetailsResponse categoryDetailsResponse = new CategoryDetailsResponse().id(UUID.fromString(categoryEntity.getUuid())).categoryName(categoryEntity.getCategoryName()).itemList(itemLists);
        return new ResponseEntity<CategoryDetailsResponse>(categoryDetailsResponse, HttpStatus.OK);

    }

    /**
     * A controller method to get all categories from the database.
     *
     * @return - ResponseEntity<CategoriesListResponse> type object along with Http status OK.
     */
    @RequestMapping(method = RequestMethod.GET, path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoriesListResponse> getAllCategories() {
        List<CategoryEntity> categoryEntityList= categoryService.getAllCategoriesOrderedByName();

        List<CategoryListResponse> categoryListResponses = new ArrayList<CategoryListResponse>();
        for(int i=0;i<categoryEntityList.size();i++){
            CategoryListResponse categoryListResponse = new CategoryListResponse().id(UUID.fromString(categoryEntityList.get(i).getUuid())).categoryName(categoryEntityList.get(i).getCategoryName());
            categoryListResponses.add(categoryListResponse);
        }

        final CategoriesListResponse categoryLists = new CategoriesListResponse().categories(categoryListResponses);
        return new ResponseEntity<CategoriesListResponse>(categoryLists, HttpStatus.OK);
    }
}
