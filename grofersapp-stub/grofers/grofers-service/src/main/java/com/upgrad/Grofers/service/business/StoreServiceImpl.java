package com.upgrad.Grofers.service.business;


import com.upgrad.Grofers.service.dao.CategoryDao;
import com.upgrad.Grofers.service.dao.StoreDao;
import com.upgrad.Grofers.service.entity.StoreEntity;
import com.upgrad.Grofers.service.exception.CategoryNotFoundException;
import com.upgrad.Grofers.service.exception.StoreNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreDao StoreDao;

    @Autowired
    private CategoryDao categoryDao;

    /**
     * The method implements the business logic for getting Store details by Store uuid.
     */
    @Override
    public StoreEntity StoreByUUID(String StoreId) throws StoreNotFoundException {
        StoreEntity storeEntity = StoreDao.storeByUUID(StoreId);
        storeEntity.setCategories(categoryDao.getAllCategoriesOrderedByName());
        return storeEntity;
    }

    /**
     * The method implements the business logic for getting Stores by Store name.
     */
    @Override
    public List<StoreEntity> StoresByName(String StoreName) throws StoreNotFoundException {
        List<StoreEntity> storeEntityList = StoreDao.storeByName(StoreName);
        for(StoreEntity storeEntity : storeEntityList)
        {
            storeEntity.setCategories(categoryDao.getAllCategoriesOrderedByName());
        }
        return storeEntityList;
    }

    /**
     * The method implements the business logic for getting all Stores ordered by their rating.
     */
    @Override
    public List<StoreEntity> StoresByRating() {
        List<StoreEntity> storeEntityList =  StoreDao.storeByRating();
        for(StoreEntity storeEntity : storeEntityList)
        {
            storeEntity.setCategories(categoryDao.getAllCategoriesOrderedByName());
        }
        return storeEntityList;
    }

    /**
     * The method implements the business logic for getting Stores by their category.
     */
    @Override
    public List<StoreEntity> StoreByCategory(String categoryId) throws CategoryNotFoundException {

        List<StoreEntity> storeEntityList = StoreDao.storeByCategory(categoryId);
        for(StoreEntity storeEntity : storeEntityList)
        {
            storeEntity.setCategories(categoryDao.getAllCategoriesOrderedByName());
        }
        return storeEntityList;
    }

}
