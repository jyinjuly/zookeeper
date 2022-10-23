package com.stockeeper.zookeeper.mapper;

import com.stockeeper.zookeeper.domain.Product;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper {

    @Select("select * from product where id = #{id}")
    Product selectProduct(Long id);

    @Update("update product set number = #{number} where id = #{id}")
    void updateProduct(Product product);

}
