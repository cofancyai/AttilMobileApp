package com.attil.inventory.data.repository

import com.attil.inventory.data.model.transaction.Indent
import com.attil.inventory.data.model.transaction.IndentItem
import com.attil.inventory.data.model.transaction.CreateIndentRequest
import com.attil.inventory.data.model.transaction.CreateIndentMainRequest
import com.attil.inventory.data.model.transaction.CreateIndentItemRequest
import com.attil.inventory.data.model.transaction.UpdateIndentRequest
import com.attil.inventory.data.model.transaction.VerifyIndentItemRequest
import com.attil.inventory.data.model.transaction.ItemForIndentSelection
import com.attil.inventory.data.model.transaction.ItemForIndent
import com.attil.inventory.data.model.transaction.CategoryForIndent
import com.attil.inventory.data.model.transaction.UpdateIndentItemRequest
import com.attil.inventory.data.model.reports.IndentReport
import com.attil.inventory.data.model.reports.IndentReportFilter
import com.attil.inventory.data.model.reports.IndentReportSummary
import com.attil.inventory.data.model.reports.IndentReportItemDetail
import com.attil.inventory.data.remote.IndentApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Singleton
class IndentRepository @Inject constructor(
    private val apiService: IndentApiService,
    private val userRepository: com.attil.inventory.data.repository.UserRepository
) {

    fun getAllIndents(): Flow<Result<List<Indent>>> = flow {
        try {
            val response = apiService.getAllIndents()
            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - getAllIndents failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - getAllIndents exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun getIndentsByChef(chefId: String): Flow<Result<List<Indent>>> = flow {
        try {
            val response = apiService.getIndentsByChef("eq.$chefId")
            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - getIndentsByChef failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - getIndentsByChef exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun getIndentsByStatus(status: String): Flow<Result<List<Indent>>> = flow {
        try {
            val response = apiService.getIndentsByStatus("eq.$status")
            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - getIndentsByStatus failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - getIndentsByStatus exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun getIndentById(id: String): Flow<Result<Indent?>> = flow {
        try {
            val response = apiService.getIndentById("eq.$id")
            if (response.isSuccessful) {
                val indents = response.body() ?: emptyList()
                emit(Result.success(indents.firstOrNull()))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - getIndentById failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - getIndentById exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun createIndent(indent: CreateIndentRequest): Flow<Result<Indent>> = flow {
        try {
            val mainIndentRequest = CreateIndentMainRequest(
                chefId = indent.chefId,
                cuisineId = indent.cuisineId,
                requiredDate = indent.requiredDate,
                requiredTime = indent.requiredTime,
                priority = indent.priority,
                purpose = indent.purpose,
                notes = indent.notes
            )

            println("DEBUG - Creating main indent: $mainIndentRequest")
            val indentResponse = apiService.createIndent(mainIndentRequest)

            if (indentResponse.isSuccessful) {
                val createdIndents = indentResponse.body() ?: emptyList()
                if (createdIndents.isNotEmpty()) {
                    val createdIndent = createdIndents.first()
                    val indentId = createdIndent.id!!

                    val indentItemRequests = indent.indentItems.map { item ->
                        CreateIndentItemRequest(
                            indentId = indentId,
                            itemId = item.itemId,
                            requestedQuantity = item.requestedQuantity,
                            unitOfMeasure = item.unitOfMeasure
                        )
                    }

                    println("DEBUG - Creating indent items: $indentItemRequests")
                    val itemsResponse = apiService.createIndentItems(indentItemRequests)

                    if (itemsResponse.isSuccessful) {
                        emit(Result.success(createdIndent))
                    } else {
                        val errorBody = itemsResponse.errorBody()?.string()
                        println("DEBUG - Items creation failed: $errorBody")
                        emit(Result.failure(Exception("Failed to create indent items: ${itemsResponse.code()} - $errorBody")))
                    }
                } else {
                    emit(Result.failure(Exception("No indent data returned")))
                }
            } else {
                val errorBody = indentResponse.errorBody()?.string()
                println("DEBUG - Indent creation failed: $errorBody")
                emit(Result.failure(Exception("Failed to create indent: ${indentResponse.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - Exception: ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    fun updateIndent(id: String, updateRequest: UpdateIndentRequest): Flow<Result<Indent>> = flow {
        try {
            val response = apiService.updateIndent("eq.$id", updateRequest)
            if (response.isSuccessful) {
                val updatedIndents = response.body() ?: emptyList()
                if (updatedIndents.isNotEmpty()) {
                    emit(Result.success(updatedIndents.first()))
                } else {
                    emit(Result.failure(Exception("No data returned from server")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - updateIndent failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - updateIndent exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun deleteIndent(id: String): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.deleteIndent("eq.$id")
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - deleteIndent failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - deleteIndent exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun getIndentItems(indentId: String): Flow<Result<List<IndentItem>>> = flow {
        try {
            val response = apiService.getIndentItems("eq.$indentId")
            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - getIndentItems failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - getIndentItems exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun updateIndentItem(id: String, updateRequest: UpdateIndentItemRequest): Flow<Result<IndentItem>> = flow {
        try {
            println("DEBUG - Updating item $id with request: $updateRequest")
            val response = apiService.updateIndentItem("eq.$id", updateRequest)
            println("DEBUG - Update response successful: ${response.isSuccessful}")
            println("DEBUG - Update response code: ${response.code()}")
            if (response.isSuccessful) {
                val updatedItems = response.body() ?: emptyList()
                println("DEBUG - Updated items count: ${updatedItems.size}")
                updatedItems.forEach { item ->
                    println("DEBUG - Updated item fulfilled quantity: ${item.fulfilledQuantity}")
                }
                if (updatedItems.isNotEmpty()) {
                    emit(Result.success(updatedItems.first()))
                } else {
                    emit(Result.failure(Exception("No data returned from server")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - updateIndentItem failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - updateIndentItem exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun verifyIndentItem(id: String, verifyRequest: VerifyIndentItemRequest): Flow<Result<IndentItem>> = flow {
        try {
            val response = apiService.verifyIndentItem("eq.$id", verifyRequest)
            if (response.isSuccessful) {
                val verifiedItems = response.body() ?: emptyList()
                if (verifiedItems.isNotEmpty()) {
                    emit(Result.success(verifiedItems.first()))
                } else {
                    emit(Result.failure(Exception("No data returned from server")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - verifyIndentItem failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - verifyIndentItem exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun verifyMultipleIndentItems(itemIds: List<String>, verifyRequest: VerifyIndentItemRequest): Flow<Result<List<IndentItem>>> = flow {
        try {
            val idsFilter = "in.(${itemIds.joinToString(",")})"
            val response = apiService.verifyMultipleIndentItems(idsFilter, verifyRequest)
            if (response.isSuccessful) {
                val verifiedItems = response.body() ?: emptyList()
                emit(Result.success(verifiedItems))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - verifyMultipleIndentItems failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - verifyMultipleIndentItems exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun getItemsForIndent(): Flow<Result<List<ItemForIndentSelection>>> = flow {
        try {
            val response = apiService.getItemsForIndent()
            if (response.isSuccessful) {
                val stockData = response.body() ?: emptyList()
                val itemsForSelection = stockData.mapNotNull { stockItem ->
                    try {
                        val itemId = stockItem["item_id"]?.toString()
                        val itemName = stockItem["item_name"]?.toString()
                        val categoryName = stockItem["category_name"]?.toString()
                        val currentStock = (stockItem["current_stock"] as? Number)?.toDouble()
                        val unitOfMeasure = stockItem["unit_of_measure"]?.toString()

                        if (itemId != null && itemName != null && currentStock != null && unitOfMeasure != null && currentStock > 0) {
                            ItemForIndentSelection(
                                item = ItemForIndent(
                                    id = itemId,
                                    name = itemName,
                                    unitOfMeasure = unitOfMeasure,
                                    categories = categoryName?.let { CategoryForIndent("", it) }
                                ),
                                availableStock = currentStock,
                                isSelected = false,
                                requestedQuantity = 0.0
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                emit(Result.success(itemsForSelection))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - getItemsForIndent failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - getItemsForIndent exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun getItemsForIndentByCategory(categoryName: String): Flow<Result<List<ItemForIndentSelection>>> = flow {
        try {
            val response = apiService.getItemsForIndentByCategory("eq.$categoryName")
            if (response.isSuccessful) {
                val stockData = response.body() ?: emptyList()
                val itemsForSelection = stockData.mapNotNull { stockItem ->
                    try {
                        val itemId = stockItem["item_id"]?.toString()
                        val itemName = stockItem["item_name"]?.toString()
                        val categoryNameFromApi = stockItem["category_name"]?.toString()
                        val currentStock = (stockItem["current_stock"] as? Number)?.toDouble()
                        val unitOfMeasure = stockItem["unit_of_measure"]?.toString()

                        if (itemId != null && itemName != null && currentStock != null && unitOfMeasure != null && currentStock > 0) {
                            ItemForIndentSelection(
                                item = ItemForIndent(
                                    id = itemId,
                                    name = itemName,
                                    unitOfMeasure = unitOfMeasure,
                                    categories = categoryNameFromApi?.let { CategoryForIndent("", it) }
                                ),
                                availableStock = currentStock,
                                isSelected = false,
                                requestedQuantity = 0.0
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                emit(Result.success(itemsForSelection))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - getItemsForIndentByCategory failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - getItemsForIndentByCategory exception: ${e.message}")
            emit(Result.failure(e))
        }
    }

    // INDENT REPORTS
    fun getIndentsForReport(filter: IndentReportFilter): Flow<Result<IndentReport>> = flow {
        try {
            // Build date range filter for PostgREST
            val dateRangeStart = "gte.${filter.startDate}"
            val dateRangeEnd = "lte.${filter.endDate}"
            val dateRange = "$dateRangeStart"

            // Build status filter
            val statusFilter = filter.status?.let { "eq.$it" }

            // Build chef filter
            val chefFilter = filter.chefId?.let { "eq.$it" }

            println("DEBUG - getIndentsForReport: dateRange=$dateRange, status=$statusFilter, chef=$chefFilter")

            // Note: We need to make two API calls with different date ranges
            // because PostgREST can't combine multiple filters on same field in single @Query
            val response = apiService.getIndentsForReport(
                dateRange = dateRangeStart,
                chefId = chefFilter,
                status = statusFilter
            )

            if (response.isSuccessful) {
                val indents = response.body() ?: emptyList()
                println("DEBUG - Fetched ${indents.size} indents")

                // Filter by end date manually (since PostgREST limitation)
                val filteredIndents = indents.filter { indent ->
                    val createdDate = indent.createdAt?.substring(0, 10) ?: ""
                    createdDate <= filter.endDate
                }

                // Fetch user names for fulfilled_by fields
                val fulfilledByUserIds = filteredIndents.mapNotNull { it.fulfilledBy }.distinct()
                val userNamesMap = mutableMapOf<String, String>()

                fulfilledByUserIds.forEach { userId ->
                    userRepository.getUserById(userId).collect { result ->
                        result.onSuccess { user ->
                            user?.let { userNamesMap[userId] = it.fullName }
                        }
                    }
                }

                // Transform to report format
                val reportSummaries = filteredIndents.map { indent ->
                    transformIndentToReportSummary(indent, userNamesMap)
                }

                val report = IndentReport(
                    reportDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                    filter = filter,
                    totalIndents = reportSummaries.size,
                    indents = reportSummaries
                )

                emit(Result.success(report))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - getIndentsForReport failed: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - getIndentsForReport exception: ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    private fun transformIndentToReportSummary(indent: Indent, userNamesMap: Map<String, String>): IndentReportSummary {
        val items = indent.indentItems ?: emptyList()

        // Calculate statistics
        val totalItems = items.size
        val fulfilledItems = items.count { (it.fulfilledQuantity ?: 0.0) > 0.0 }
        val verifiedItems = items.count { it.isReceived == true }
        val rejectedItems = items.count { it.isReceived == false }

        // Get user names
        val chefName = indent.chef?.fullName ?: "Unknown"
        val cuisineName = indent.cuisines?.name ?: "Unknown"
        val fulfilledByName = indent.fulfilledBy?.let { userNamesMap[it] }

        // Transform items to report detail
        val itemDetails = items.map { item ->
            IndentReportItemDetail(
                itemName = item.items?.name ?: "Unknown",
                requestedQuantity = item.requestedQuantity,
                approvedQuantity = item.approvedQuantity,
                fulfilledQuantity = item.fulfilledQuantity,
                unitOfMeasure = item.unitOfMeasure,
                itemStatus = item.status,
                isFulfilled = (item.fulfilledQuantity ?: 0.0) > 0.0,
                isVerified = item.isReceived == true,
                isRejected = item.isReceived == false,
                receivedBy = item.receivedBy,
                receivedAt = item.receivedAt
            )
        }

        return IndentReportSummary(
            indentId = indent.id ?: "",
            chefName = chefName,
            cuisineName = cuisineName,
            requiredDate = indent.requiredDate,
            requiredTime = indent.requiredTime,
            priority = indent.priority,
            purpose = indent.purpose,
            status = indent.status,
            createdAt = indent.createdAt ?: "",
            fulfilledBy = fulfilledByName,
            fulfilledAt = indent.fulfilledAt,
            totalItems = totalItems,
            fulfilledItems = fulfilledItems,
            verifiedItems = verifiedItems,
            rejectedItems = rejectedItems,
            items = itemDetails
        )
    }
}