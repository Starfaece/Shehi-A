package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    fun getSaleWithItemsById(id: Long): Flow<SaleWithItems?>

    @Transaction
    @Query("SELECT * FROM sales WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getSalesBetweenDates(startTime: Long, endTime: Long): Flow<List<SaleWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItems(): Flow<List<SaleItemEntity>>

    @Query("DELETE FROM sales")
    suspend fun clearAllSales()

    @Query("DELETE FROM sale_items")
    suspend fun clearAllSaleItems()
}
