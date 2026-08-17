package com.example.ui.resep

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MealPlanItem
import com.example.data.entity.Recipe
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.theme.SoftCreamCanvas
import com.example.ui.theme.SoftTextDark
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CATEGORIES = emptyList<String>()

private val DAYS_OF_WEEK = listOf(
    "Senin",
    "Selasa",
    "Rabu",
    "Kamis",
    "Jumat",
    "Sabtu",
    "Minggu"
)

private val MEAL_TYPES = listOf(
    "Sarapan",
    "Makan Siang",
    "Makan Malam",
    "Cemilan"
)

private val FLAVOR_TAGS = listOf(
    "#TidakPedas",
    "#Sedang",
    "#SuperPedas",
    "#GurihManis",
    "#AsamSegar",
    "#AromaRempah"
)

private fun scaleIngredientLine(line: String, multiplier: Float): String {
    if (multiplier == 1.0f || line.isBlank()) return line
    val regex = Regex("""(?:\b(\d+)\s+)?(\d+)/(\d+)|\b(\d+(?:\.\d+)?)""")
    return regex.replace(line) { match ->
        val wholePart = match.groups[1]?.value?.toFloatOrNull() ?: 0f
        val num = match.groups[2]?.value?.toFloatOrNull()
        val den = match.groups[3]?.value?.toFloatOrNull()
        val simpleNum = match.groups[4]?.value?.toFloatOrNull()

        val value = if (num != null && den != null && den != 0f) {
            (wholePart + (num / den)) * multiplier
        } else if (simpleNum != null) {
            simpleNum * multiplier
        } else {
            return@replace match.value
        }

        if (value % 1.0f == 0.0f) {
            value.toInt().toString()
        } else {
            String.format(java.util.Locale.US, "%.1f", value).removeSuffix(".0")
        }
    }
}

private fun android.content.Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResepScreen(
    activeRecipes: List<Recipe>,
    deletedRecipes: List<Recipe>,
    mealPlanItems: List<MealPlanItem>,
    onAddRecipe: (title: String, description: String, category: String, prepTime: String, cookTime: String, yields: String, ingredients: String, directions: String, skillRating: Int, isFavorite: Boolean, flavorTag: String, source: String) -> Unit,
    onUpdateRecipe: (Recipe) -> Unit,
    onSoftDeleteRecipe: (Int) -> Unit,
    onRestoreRecipe: (Int) -> Unit,
    onHardDeleteRecipe: (Int) -> Unit,
    onClearTrashRecipes: () -> Unit,
    onAddMealPlanItem: (dayOfWeek: String, recipeId: Int, recipeTitle: String, mealType: String) -> Unit,
    onDeleteMealPlanItem: (Int) -> Unit,
    onClearMealPlanForDay: (String) -> Unit,
    onExportWeeklyMealPlanToShoppingList: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Rencana Masak, 1: Resep (Pusat), 2: Recent Delete

    // Dialog state for adding/editing recipe
    var showAddEditRecipeDialog by remember { mutableStateOf(false) }
    var recipeToEdit by remember { mutableStateOf<Recipe?>(null) }

    // Dialog state for viewing full recipe detail
    var recipeToDetailView by remember { mutableStateOf<Recipe?>(null) }

    // Dialog state for adding recipe to meal plan
    var dayForMealPlanAssignment by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Buku Resep Dapur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SoftTextDark
                        )
                        Text(
                            text = "Catatan Resep Murni Teks & Rencana Masak",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_resep")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = SoftTextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftCreamCanvas
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                ExtendedFloatingActionButton(
                    onClick = {
                        recipeToEdit = null
                        showAddEditRecipeDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Resep Baru", fontWeight = FontWeight.Bold) },
                    containerColor = SageGreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_recipe")
                )
            }
        },
        containerColor = SoftCreamCanvas
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Header Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = SageGreenPrimary,
                edgePadding = 8.dp,
                modifier = Modifier.testTag("resep_tab_row")
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "Rencana Masak",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_rencana_masak")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Resep (${activeRecipes.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_kumpulan_resep")
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            "Statistik & Grafik",
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_statistik_resep")
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = {
                        Text(
                            "Sampah (${deletedRecipes.size})",
                            fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_sampah_resep")
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    TabRencanaMasakContent(
                        activeRecipes = activeRecipes,
                        mealPlanItems = mealPlanItems,
                        onOpenSelectRecipeForDay = { day -> dayForMealPlanAssignment = day },
                        onDeleteMealPlanItem = onDeleteMealPlanItem,
                        onClearDay = onClearMealPlanForDay,
                        onExportToShoppingList = {
                            onExportWeeklyMealPlanToShoppingList()
                            Toast.makeText(context, "Seluruh bahan resep mingguan berhasil dikirim ke Daftar Belanja!", Toast.LENGTH_LONG).show()
                        }
                    )
                }
                1 -> {
                    TabKumpulanResepContent(
                        recipes = activeRecipes,
                        onRecipeClick = { recipeToDetailView = it },
                        onEditClick = { recipe ->
                            recipeToEdit = recipe
                            showAddEditRecipeDialog = true
                        },
                        onDeleteClick = { recipeId ->
                            onSoftDeleteRecipe(recipeId)
                            Toast.makeText(context, "Resep dipindahkan ke Sampah", Toast.LENGTH_SHORT).show()
                        },
                        onToggleFavorite = { recipe ->
                            onUpdateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))
                        },
                        onAddToMealPlanClick = { recipe ->
                            dayForMealPlanAssignment = "Senin"
                        }
                    )
                }
                2 -> {
                    TabStatistikContent(
                        activeRecipes = activeRecipes,
                        mealPlanItems = mealPlanItems,
                        onRecipeClick = { recipeToDetailView = it }
                    )
                }
                3 -> {
                    TabSampahResepContent(
                        deletedRecipes = deletedRecipes,
                        onRestore = { recipeId ->
                            onRestoreRecipe(recipeId)
                            Toast.makeText(context, "Resep berhasil dipulihkan", Toast.LENGTH_SHORT).show()
                        },
                        onPermanentDelete = { recipeId ->
                            onHardDeleteRecipe(recipeId)
                            Toast.makeText(context, "Resep dihapus permanen", Toast.LENGTH_SHORT).show()
                        },
                        onClearAllTrash = {
                            onClearTrashRecipes()
                            Toast.makeText(context, "Sampah resep dibersihkan", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // --- DIALOGS ---

    // Add / Edit Recipe Dialog
    if (showAddEditRecipeDialog) {
        val dynamicCategories = activeRecipes.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        AddEditRecipeDialog(
            existingRecipe = recipeToEdit,
            availableCategories = dynamicCategories,
            onDismiss = { showAddEditRecipeDialog = false },
            onSave = { title, desc, cat, prep, cook, yield, ingr, dir, skill, fav, flavor, src ->
                if (recipeToEdit == null) {
                    onAddRecipe(title, desc, cat, prep, cook, yield, ingr, dir, skill, fav, flavor, src)
                    Toast.makeText(context, "Resep baru berhasil disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    val updated = recipeToEdit!!.copy(
                        title = title,
                        description = desc,
                        category = cat,
                        prepTime = prep,
                        cookTime = cook,
                        yields = yield,
                        ingredients = ingr,
                        directions = dir,
                        skillRating = skill,
                        isFavorite = fav,
                        flavorTag = flavor,
                        source = src
                    )
                    onUpdateRecipe(updated)
                    Toast.makeText(context, "Perubahan resep berhasil disimpan", Toast.LENGTH_SHORT).show()
                }
                showAddEditRecipeDialog = false
            }
        )
    }

    // View Recipe Detail Dialog
    if (recipeToDetailView != null) {
        RecipeDetailModal(
            recipe = recipeToDetailView!!,
            activeRecipes = activeRecipes,
            onDismiss = { recipeToDetailView = null },
            onEdit = {
                val r = recipeToDetailView!!
                recipeToDetailView = null
                recipeToEdit = r
                showAddEditRecipeDialog = true
            },
            onDelete = {
                val id = recipeToDetailView!!.id
                recipeToDetailView = null
                onSoftDeleteRecipe(id)
                Toast.makeText(context, "Resep dipindahkan ke Sampah", Toast.LENGTH_SHORT).show()
            },
            onToggleFavorite = { updatedRecipe ->
                onUpdateRecipe(updatedRecipe)
                recipeToDetailView = updatedRecipe
            },
            onAddMealPlanItem = { day, recipeId, recipeTitle, mealType ->
                onAddMealPlanItem(day, recipeId, recipeTitle, mealType)
            }
        )
    }

    // Select Recipe for Day Dialog
    if (dayForMealPlanAssignment != null) {
        AssignRecipeToDayDialog(
            dayName = dayForMealPlanAssignment!!,
            activeRecipes = activeRecipes,
            onDismiss = { dayForMealPlanAssignment = null },
            onAssign = { recipeId, recipeTitle, mealType ->
                onAddMealPlanItem(dayForMealPlanAssignment!!, recipeId, recipeTitle, mealType)
                Toast.makeText(context, "Resep ditambahkan ke rencana $dayForMealPlanAssignment", Toast.LENGTH_SHORT).show()
                dayForMealPlanAssignment = null
            }
        )
    }
}

// ---------------------------------------------------------------------
// TAB 1: KALENDER MINGGUAN (RENCANA MASAK)
// ---------------------------------------------------------------------
@Composable
private fun TabRencanaMasakContent(
    activeRecipes: List<Recipe>,
    mealPlanItems: List<MealPlanItem>,
    onOpenSelectRecipeForDay: (String) -> Unit,
    onDeleteMealPlanItem: (Int) -> Unit,
    onClearDay: (String) -> Unit,
    onExportToShoppingList: () -> Unit
) {
    val totalPlanned = mealPlanItems.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Header & Export Button Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SageGreenPrimaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_summary_meal_plan")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SageGreenPrimary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Rencana Masak Mingguan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = SoftTextDark
                                )
                                Text(
                                    text = "$totalPlanned Menu Tersusun Minggu Ini",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = "Susun menu harian dan klik tombol di bawah untuk menyatukan seluruh bahan dari resep ke Daftar Belanja.",
                        fontSize = 12.sp,
                        color = SoftTextDark
                    )

                    Button(
                        onClick = onExportToShoppingList,
                        enabled = totalPlanned > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_mealplan_shopping"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SageGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🛒 Export Seluruh Bahan ke Daftar Belanja",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // List of Days
        items(DAYS_OF_WEEK) { day ->
            val dayItems = mealPlanItems.filter { it.dayOfWeek.equals(day, ignoreCase = true) }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("day_card_$day")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SageGreenPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = day,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SageGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            if (dayItems.isNotEmpty()) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = "${dayItems.size} Resep",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SageGreenPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (dayItems.isNotEmpty()) {
                                IconButton(
                                    onClick = { onClearDay(day) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = "Bersihkan Hari",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { onOpenSelectRecipeForDay(day) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_add_recipe_to_$day")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Resep", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (dayItems.isEmpty()) {
                        Text(
                            text = "Belum ada rencana masakan. Klik '+ Resep' untuk menyematkan resep.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dayItems.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SoftCreamCanvas,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFFFF3E0)
                                            ) {
                                                Text(
                                                    text = item.mealType,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE65100),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = item.recipeTitle,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = SoftTextDark,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteMealPlanItem(item.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Hapus Menu",
                                                tint = Color.Red.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// TAB 2: KUMPULAN RESEP (PUSAT CATATAN MURNI TEKS)
// ---------------------------------------------------------------------
@Composable
private fun TabKumpulanResepContent(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onEditClick: (Recipe) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onToggleFavorite: (Recipe) -> Unit,
    onAddToMealPlanClick: (Recipe) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showCategoryFilterMenu by remember { mutableStateOf(false) }

    // Dynamic categories list based strictly on user's existing recipes
    val filterCategories = remember(recipes) {
        val recipeCategories = recipes.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        listOf("Semua", "Favorit") + recipeCategories
    }

    val filteredRecipes = remember(recipes, searchQuery, selectedCategory) {
        recipes.filter { recipe ->
            val matchesSearch = recipe.title.contains(searchQuery, ignoreCase = true) ||
                    recipe.description.contains(searchQuery, ignoreCase = true) ||
                    recipe.ingredients.contains(searchQuery, ignoreCase = true) ||
                    recipe.flavorTag.contains(searchQuery, ignoreCase = true) ||
                    recipe.source.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                "Semua" -> true
                "Favorit" -> recipe.isFavorite
                else -> recipe.category.equals(selectedCategory, ignoreCase = true)
            }
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar + Filter Icon Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari judul resep") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SageGreenPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_search_resep"),
                singleLine = true
            )

            // Filter Button with Dropdown Menu
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedCategory != "Semua") SageGreenPrimary else Color.White,
                    border = if (selectedCategory != "Semua") null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { showCategoryFilterMenu = true }
                        .testTag("btn_filter_category_resep")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Kategori",
                            tint = if (selectedCategory != "Semua") Color.White else SageGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showCategoryFilterMenu,
                    onDismissRequest = { showCategoryFilterMenu = false },
                    modifier = Modifier
                        .background(Color.White)
                        .widthIn(min = 190.dp)
                ) {
                    Text(
                        text = "Filter Kategori",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SageGreenPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                    HorizontalDivider()

                    filterCategories.forEach { cat ->
                        val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = cat,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SageGreenPrimary else SoftTextDark,
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SageGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = {
                                selectedCategory = cat
                                showCategoryFilterMenu = false
                            },
                            modifier = Modifier.testTag("filter_menu_item_$cat")
                        )
                    }
                }
            }
        }

        // Active Category Filter Indicator Chip
        if (selectedCategory != "Semua") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SageGreenPrimaryContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Kategori: $selectedCategory",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus Filter Kategori",
                            tint = SageGreenPrimary,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { selectedCategory = "Semua" }
                        )
                    }
                }
            }
        }

        // Recipes List
        if (filteredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedCategory != "Semua") "Resep tidak ditemukan" else "Belum Ada Catatan Resep",
                        fontWeight = FontWeight.Bold,
                        color = SoftTextDark
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedCategory != "Semua")
                            "Coba ubah kata kunci pencarian atau filter kategori."
                        else
                            "Belum ada resep & kategori tersimpan. Klik tombol '+ Resep Baru' di atas untuk mulai membuat resep dan kategori pertama Anda!",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredRecipes) { recipe ->
                    RecipeCardTextOnly(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) },
                        onEdit = { onEditClick(recipe) },
                        onDelete = { onDeleteClick(recipe.id) },
                        onToggleFavorite = { onToggleFavorite(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeCardTextOnly(
    recipe: Recipe,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("recipe_card_${recipe.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Category Badge + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SageGreenPrimaryContainer
                    ) {
                        Text(
                            text = recipe.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (recipe.flavorTag.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = recipe.flavorTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_favorite_recipe_${recipe.id}")
                    ) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (recipe.isFavorite) Color(0xFFE91E63) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_edit_recipe_${recipe.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Resep", tint = SageGreenPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_delete_recipe_${recipe.id}")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Resep", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Title + Rating Stars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SoftTextDark,
                    modifier = Modifier.weight(1f)
                )

                if (recipe.skillRating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(recipe.skillRating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Description
            if (recipe.description.isNotBlank()) {
                Text(
                    text = recipe.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Badges Row: Time, Yields, Source
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (recipe.prepTime.isNotBlank() || recipe.cookTime.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(
                            text = listOfNotNull(recipe.prepTime.takeIf { it.isNotBlank() }?.let { "Prep $it" }, recipe.cookTime.takeIf { it.isNotBlank() }?.let { "Masak $it" }).joinToString(" • "),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (recipe.yields.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(text = recipe.yields, fontSize = 11.sp, color = Color.Gray)
                    }
                }

                if (recipe.source.isNotBlank()) {
                    Text(
                        text = "Sumber: ${recipe.source}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = SoftCreamCanvas, thickness = 1.dp)

            // Click hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val ingredientCount = recipe.ingredients.split("\n").filter { it.isNotBlank() }.size
                Text(
                    text = "$ingredientCount Bahan Terdaftar",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SageGreenPrimary
                )
                Text(
                    text = "Lihat Resep Lengkap >",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SageGreenPrimary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// TAB 3: SAMPAH RESEP (RECENT DELETE)
// ---------------------------------------------------------------------
@Composable
private fun TabSampahResepContent(
    deletedRecipes: List<Recipe>,
    onRestore: (Int) -> Unit,
    onPermanentDelete: (Int) -> Unit,
    onClearAllTrash: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (deletedRecipes.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Resep Dihapus Sementara",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftTextDark
                )

                OutlinedButton(
                    onClick = onClearAllTrash,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_clear_trash")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kosongkan Sampah", fontSize = 12.sp)
                }
            }
        }

        if (deletedRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Sampah Kosong",
                        fontWeight = FontWeight.Bold,
                        color = SoftTextDark
                    )
                    Text(
                        text = "Tidak ada resep yang berada di tempat sampah.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(deletedRecipes) { recipe ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trash_item_${recipe.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = recipe.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SoftTextDark
                            )

                            if (recipe.description.isNotBlank()) {
                                Text(
                                    text = recipe.description,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onRestore(recipe.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreenPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_restore_${recipe.id}")
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pulihkan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { onPermanentDelete(recipe.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_hard_delete_${recipe.id}")
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hapus Permanen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// FORM DIALOG: TAMBAH / EDIT RESEP
// ---------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditRecipeDialog(
    existingRecipe: Recipe?,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, cat: String, prep: String, cook: String, yield: String, ingr: String, dir: String, skillRating: Int, isFavorite: Boolean, flavorTag: String, source: String) -> Unit
) {
    var title by remember { mutableStateOf(existingRecipe?.title ?: "") }
    var description by remember { mutableStateOf(existingRecipe?.description ?: "") }
    var categoryInput by remember { mutableStateOf(existingRecipe?.category ?: "") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var customCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var prepTime by remember { mutableStateOf(existingRecipe?.prepTime ?: "") }
    var cookTime by remember { mutableStateOf(existingRecipe?.cookTime ?: "") }
    var yields by remember { mutableStateOf(existingRecipe?.yields ?: "") }
    var skillRating by remember { mutableIntStateOf(existingRecipe?.skillRating ?: 0) }
    var isFavorite by remember { mutableStateOf(existingRecipe?.isFavorite ?: false) }
    var flavorTag by remember { mutableStateOf(existingRecipe?.flavorTag ?: "") }
    var source by remember { mutableStateOf(existingRecipe?.source ?: "") }

    val allCategoriesList = remember(availableCategories, customCategories) {
        (availableCategories + customCategories).filter { it != "Semua" && it.isNotBlank() }.distinct()
    }

    val filteredCategories = remember(allCategoriesList, categoryInput) {
        if (categoryInput.isBlank()) {
            allCategoriesList
        } else {
            allCategoriesList.filter { it.contains(categoryInput, ignoreCase = true) }
        }
    }

    val isExactMatch = remember(allCategoriesList, categoryInput) {
        val trimmed = categoryInput.trim()
        allCategoriesList.any { it.equals(trimmed, ignoreCase = true) }
    }

    val showAddNewCategoryOption = categoryInput.trim().isNotBlank() && !isExactMatch

    // Dynamic Ingredients list
    val ingredientsList = remember {
        mutableStateListOf<String>().apply {
            if (existingRecipe != null && existingRecipe.ingredients.isNotBlank()) {
                addAll(existingRecipe.ingredients.split("\n").filter { it.isNotBlank() })
            } else {
                add("") // Start with one empty row
            }
        }
    }

    // Dynamic Directions list
    val directionsList = remember {
        mutableStateListOf<String>().apply {
            if (existingRecipe != null && existingRecipe.directions.isNotBlank()) {
                addAll(existingRecipe.directions.split("\n").filter { it.isNotBlank() })
            } else {
                add("") // Start with one empty step
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingRecipe == null) "Tambah Resep Baru" else "Edit Resep",
                    fontWeight = FontWeight.Bold,
                    color = SoftTextDark
                )

                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier.testTag("btn_toggle_favorite_in_dialog")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorit",
                        tint = if (isFavorite) Color(0xFFE91E63) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Judul
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Resep *") },
                        enabled = true,
                        readOnly = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_recipe_title"),
                        singleLine = true
                    )
                }

                // Catatan Singkat
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Catatan Singkat / Deskripsi") },
                        enabled = true,
                        readOnly = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_recipe_desc"),
                        singleLine = true
                    )
                }

                // Skill Rating Stars (Ukur Kemampuan Penguasaan Resep)
                item {
                    Text("Rating Penguasaan Masak:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SoftTextDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { skillRating = if (skillRating == star) 0 else star },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("input_star_rating_$star")
                            ) {
                                Icon(
                                    imageVector = if (star <= skillRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star Bintang",
                                    tint = if (star <= skillRating) Color(0xFFFFB300) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    if (skillRating > 0) {
                        Text(
                            text = when (skillRating) {
                                1 -> "★1 - Pemula / Belum Hafal"
                                2 -> "★2 - Cukup Bisa"
                                3 -> "★3 - Lumayan Enak"
                                4 -> "★4 - Sangat Mahir"
                                5 -> "★5 - Menu Juara Keluarga!"
                                else -> ""
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )
                    }
                }

                // Flavor Tags
                item {
                    Text("Profil Rasa / Level Pedas:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SoftTextDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(FLAVOR_TAGS) { tag ->
                            val isSelected = flavorTag.contains(tag, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    flavorTag = if (isSelected) {
                                        flavorTag.replace(tag, "").trim()
                                    } else {
                                        if (flavorTag.isBlank()) tag else "$flavorTag $tag"
                                    }
                                },
                                label = { Text(tag, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = flavorTag,
                        onValueChange = { flavorTag = it },
                        label = { Text("Tag Rasa Custom (mis. #TidakPedas)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_recipe_flavor_tag"),
                        singleLine = true
                    )
                }

                // Source field
                item {
                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text("Sumber Resep (Opsional)") },
                        placeholder = { Text("mis. Resep Ibu, TikTok, Rudy Choirudin") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_recipe_source"),
                        singleLine = true
                    )
                }

                // Kategori Selector (Combobox / Dropdown + Custom Input)
                item {
                    Text("Kategori Resep:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SoftTextDark)

                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = {
                                categoryInput = it
                                categoryDropdownExpanded = true
                            },
                            label = { Text("Pilih / Ketik Kategori *") },
                            placeholder = { Text("mis. Masakan Harian, Sup, Sambal...") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("input_recipe_category_combobox"),
                            singleLine = true
                        )

                        if (filteredCategories.isNotEmpty() || showAddNewCategoryOption) {
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .heightIn(max = 240.dp)
                            ) {
                                filteredCategories.forEach { cat ->
                                    val isSelected = cat.equals(categoryInput.trim(), ignoreCase = true)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = cat,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) SageGreenPrimary else SoftTextDark,
                                                fontSize = 13.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Category,
                                                contentDescription = null,
                                                tint = if (isSelected) SageGreenPrimary else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            categoryInput = cat
                                            categoryDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("category_option_$cat")
                                    )
                                }

                                if (showAddNewCategoryOption) {
                                    if (filteredCategories.isNotEmpty()) {
                                        HorizontalDivider()
                                    }
                                    val newCategoryName = categoryInput.trim()
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "(+ Kategori Baru: \"$newCategoryName\")",
                                                fontWeight = FontWeight.Bold,
                                                color = SageGreenPrimary,
                                                fontSize = 13.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.AddCircleOutline,
                                                contentDescription = null,
                                                tint = SageGreenPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            if (!customCategories.contains(newCategoryName)) {
                                                customCategories = customCategories + newCategoryName
                                            }
                                            categoryInput = newCategoryName
                                            categoryDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("btn_add_new_category_option")
                                    )
                                }
                            }
                        }
                    }
                }

                // Time & Yield
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = prepTime,
                            onValueChange = { prepTime = it },
                            label = { Text("Persiapan (mis. 15 mnt)", fontSize = 11.sp) },
                            enabled = true,
                            readOnly = false,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_recipe_preptime"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = cookTime,
                            onValueChange = { cookTime = it },
                            label = { Text("Masak (mis. 30 mnt)", fontSize = 11.sp) },
                            enabled = true,
                            readOnly = false,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_recipe_cooktime"),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = yields,
                        onValueChange = { yields = it },
                        label = { Text("Porsi / Hasil (mis. 4 Porsi)") },
                        enabled = true,
                        readOnly = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_recipe_yields"),
                        singleLine = true
                    )
                }

                // Section: Ingredients (Bahan-Bahan)
                item {
                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                    Text(
                        text = "Bahan-Bahan (Ingredients)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SageGreenPrimary
                    )
                }

                itemsIndexed(ingredientsList) { index, itemText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = itemText,
                            onValueChange = { newText ->
                                ingredientsList[index] = newText
                            },
                            label = { Text("Bahan #${index + 1}") },
                            enabled = true,
                            readOnly = false,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_ingredient_$index"),
                            singleLine = true
                        )

                        IconButton(
                            onClick = { ingredientsList.removeAt(index) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus Bahan", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { ingredientsList.add("") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_add_ingredient_row"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Tambah Baris Bahan", fontSize = 12.sp)
                    }
                }

                // Section: Directions (Langkah Pembuatan)
                item {
                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                    Text(
                        text = "Langkah-Langkah Pembuatan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SageGreenPrimary
                    )
                }

                itemsIndexed(directionsList) { index, stepText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = stepText,
                            onValueChange = { newText ->
                                directionsList[index] = newText
                            },
                            label = { Text("Langkah #${index + 1}") },
                            enabled = true,
                            readOnly = false,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_direction_$index")
                        )

                        IconButton(
                            onClick = { directionsList.removeAt(index) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus Langkah", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { directionsList.add("") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_add_direction_row"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Tambah Baris Langkah", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val ingrFormatted = ingredientsList.filter { it.isNotBlank() }.joinToString("\n")
                    val dirFormatted = directionsList.filter { it.isNotBlank() }.joinToString("\n")

                    val finalCategory = categoryInput.trim().ifBlank { "Lainnya" }
                    onSave(
                        title.trim(),
                        description.trim(),
                        finalCategory,
                        prepTime.trim(),
                        cookTime.trim(),
                        yields.trim(),
                        ingrFormatted,
                        dirFormatted,
                        skillRating,
                        isFavorite,
                        flavorTag.trim(),
                        source.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("btn_save_recipe")
            ) {
                Text("Simpan Resep", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ---------------------------------------------------------------------
// MODAL: VIEW FULL RECIPE DETAIL
// ---------------------------------------------------------------------
@Composable
private fun RecipeDetailModal(
    recipe: Recipe,
    activeRecipes: List<Recipe>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: (Recipe) -> Unit,
    onAddMealPlanItem: (dayOfWeek: String, recipeId: Int, recipeTitle: String, mealType: String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var keepScreenOn by remember { mutableStateOf(false) }
    var servingsMultiplier by remember { mutableIntStateOf(1) }
    var showMealPlanDialog by remember { mutableStateOf(false) }

    // Keep Screen On Effect
    DisposableEffect(keepScreenOn) {
        val activity = context.findActivity()
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SageGreenPrimaryContainer
                        ) {
                            Text(
                                text = recipe.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        if (recipe.flavorTag.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = recipe.flavorTag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { onToggleFavorite(recipe) },
                            modifier = Modifier.testTag("btn_favorite_in_modal")
                        ) {
                            Icon(
                                imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorit",
                                tint = if (recipe.isFavorite) Color(0xFFE91E63) else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                }

                Text(
                    text = recipe.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SoftTextDark
                )

                if (recipe.skillRating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(recipe.skillRating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = " (${recipe.skillRating}/5)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Action Buttons Bar: Keep Screen On, Copy, Add to Meal Plan
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Keep Screen On Toggle Button
                        FilterChip(
                            selected = keepScreenOn,
                            onClick = { keepScreenOn = !keepScreenOn },
                            label = {
                                Text(
                                    text = if (keepScreenOn) "Layar Nyala" else "Layar Mati",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (keepScreenOn) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (keepScreenOn) Color(0xFFFFB300) else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFF8E1),
                                selectedLabelColor = Color(0xFFE65100)
                            ),
                            modifier = Modifier.testTag("btn_keep_screen_on")
                        )

                        // Copy Recipe Button
                        OutlinedButton(
                            onClick = {
                                val textToCopy = buildString {
                                    appendLine("🍳 ${recipe.title}")
                                    if (recipe.category.isNotBlank()) appendLine("Kategori: ${recipe.category}")
                                    if (recipe.source.isNotBlank()) appendLine("Sumber: ${recipe.source}")
                                    if (recipe.flavorTag.isNotBlank()) appendLine("Profil Rasa: ${recipe.flavorTag}")
                                    if (recipe.prepTime.isNotBlank() || recipe.cookTime.isNotBlank()) {
                                        appendLine("Waktu: Prep ${recipe.prepTime} | Masak ${recipe.cookTime}")
                                    }
                                    appendLine("Porsi: ${recipe.yields.ifBlank { "Standard" }} (Skala: ${servingsMultiplier}x)")
                                    appendLine("\n--- BAHAN-BAHAN ---")
                                    val ingr = recipe.ingredients.split("\n").filter { it.isNotBlank() }
                                    if (ingr.isEmpty()) appendLine("(Belum ada bahan)")
                                    else ingr.forEach { appendLine("• ${scaleIngredientLine(it, servingsMultiplier.toFloat())}") }

                                    appendLine("\n--- LANGKAH PEMBUATAN ---")
                                    val dir = recipe.directions.split("\n").filter { it.isNotBlank() }
                                    if (dir.isEmpty()) appendLine("(Belum ada langkah)")
                                    else dir.forEachIndexed { idx, step -> appendLine("${idx + 1}. $step") }
                                }
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, "Seluruh resep berhasil disalin!", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("btn_copy_recipe")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Add to Meal Plan Button
                        Button(
                            onClick = { showMealPlanDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                            modifier = Modifier.testTag("btn_add_to_meal_plan_from_detail")
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rencana Masak", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Description & Source
                if (recipe.description.isNotBlank() || recipe.source.isNotBlank()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            if (recipe.description.isNotBlank()) {
                                Text(
                                    text = recipe.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (recipe.source.isNotBlank()) {
                                Text(
                                    text = "Sumber: ${recipe.source}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Info Chips
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (recipe.prepTime.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Prep: ${recipe.prepTime}", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        if (recipe.cookTime.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Masak: ${recipe.cookTime}", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.SoupKitchen, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        if (recipe.yields.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text(recipe.yields, fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                    }
                }

                // Servings / Yield Multiplier Stepper
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftCreamCanvas,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Skala Porsi Otomatis:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftTextDark
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { if (servingsMultiplier > 1) servingsMultiplier -= 1 },
                                    enabled = servingsMultiplier > 1,
                                    modifier = Modifier.size(32.dp).testTag("btn_decrease_yield")
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Kurangi Porsi", tint = if (servingsMultiplier > 1) SageGreenPrimary else Color.Gray)
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = "${servingsMultiplier}x",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SageGreenPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { if (servingsMultiplier < 10) servingsMultiplier += 1 },
                                    enabled = servingsMultiplier < 10,
                                    modifier = Modifier.size(32.dp).testTag("btn_increase_yield")
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Tambah Porsi", tint = if (servingsMultiplier < 10) SageGreenPrimary else Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Ingredients List (Scaled)
                item {
                    Text(
                        text = "Bahan-Bahan (Takaran Disesuaikan):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SageGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val ingrLines = recipe.ingredients.split("\n").filter { it.isNotBlank() }
                    if (ingrLines.isEmpty()) {
                        Text("Belum ada bahan tercatat.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ingrLines.forEach { line ->
                                val scaledLine = scaleIngredientLine(line, servingsMultiplier.toFloat())
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("•", fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                                    Text(
                                        text = scaledLine,
                                        fontSize = 12.sp,
                                        color = SoftTextDark,
                                        fontWeight = if (servingsMultiplier > 1) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Directions List
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Langkah Pembuatan:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SageGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val dirLines = recipe.directions.split("\n").filter { it.isNotBlank() }
                    if (dirLines.isEmpty()) {
                        Text("Belum ada langkah pembuatan tercatat.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            dirLines.forEachIndexed { index, step ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SageGreenPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SageGreenPrimary
                                            )
                                        }
                                    }
                                    Text(text = step, fontSize = 12.sp, color = SoftTextDark)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )

    // Sub-dialog: Assign Recipe to Meal Plan Day
    if (showMealPlanDialog) {
        var selectedDay by remember { mutableStateOf(DAYS_OF_WEEK.first()) }
        var selectedMealType by remember { mutableStateOf(MEAL_TYPES.first()) }

        AlertDialog(
            onDismissRequest = { showMealPlanDialog = false },
            title = {
                Text(
                    text = "Tambah ke Rencana Masak",
                    fontWeight = FontWeight.Bold,
                    color = SoftTextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Resep: ${recipe.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SageGreenPrimary
                    )

                    Text("Pilih Hari:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(DAYS_OF_WEEK) { day ->
                            FilterChip(
                                selected = selectedDay == day,
                                onClick = { selectedDay = day },
                                label = { Text(day, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Waktu Makan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MEAL_TYPES) { meal ->
                            FilterChip(
                                selected = selectedMealType == meal,
                                onClick = { selectedMealType = meal },
                                label = { Text(meal, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddMealPlanItem(selectedDay, recipe.id, recipe.title, selectedMealType)
                        Toast.makeText(context, "${recipe.title} dijadwalkan untuk $selectedDay ($selectedMealType)", Toast.LENGTH_SHORT).show()
                        showMealPlanDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                ) {
                    Text("Jadwalkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMealPlanDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ---------------------------------------------------------------------
// DIALOG: ASSIGN RECIPE TO MEAL PLAN DAY
// ---------------------------------------------------------------------
@Composable
private fun AssignRecipeToDayDialog(
    dayName: String,
    activeRecipes: List<Recipe>,
    onDismiss: () -> Unit,
    onAssign: (recipeId: Int, recipeTitle: String, mealType: String) -> Unit
) {
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var selectedMealType by remember { mutableStateOf("Makan Siang") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pilih Resep untuk Hari $dayName",
                fontWeight = FontWeight.Bold,
                color = SoftTextDark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pilih Waktu Makan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MEAL_TYPES.forEach { type ->
                        val isSel = selectedMealType == type
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedMealType = type },
                            label = { Text(type, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Divider()

                Text("Pilih Resep Tersimpan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                if (activeRecipes.isEmpty()) {
                    Text(
                        text = "Belum ada resep tersimpan di Tab Resep.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(activeRecipes) { recipe ->
                            val isSelected = selectedRecipe?.id == recipe.id
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) SageGreenPrimaryContainer else SoftCreamCanvas
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRecipe = recipe }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = recipe.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = SoftTextDark
                                        )
                                        Text(
                                            text = recipe.category,
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SageGreenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRecipe != null) {
                        onAssign(selectedRecipe!!.id, selectedRecipe!!.title, selectedMealType)
                    }
                },
                enabled = selectedRecipe != null,
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Sematkan Resep", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ==========================================
// STATISTIK & GRAFIK MASAK MODULE
// ==========================================

data class ExperimentNote(
    val id: Long = System.currentTimeMillis(),
    val recipeTitle: String,
    val date: String,
    val status: String, // "Perlu Koreksi", "Tips & Improvisasi", "Eksperimen Sukses"
    val note: String
)

private fun loadExperimentNotes(context: Context): List<ExperimentNote> {
    val prefs = context.getSharedPreferences("dipta_recipe_experiments", Context.MODE_PRIVATE)
    val jsonStr = prefs.getString("notes_json", "[]") ?: "[]"
    val list = mutableListOf<ExperimentNote>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ExperimentNote(
                    id = obj.optLong("id", System.currentTimeMillis()),
                    recipeTitle = obj.optString("recipeTitle", "Resep Umum"),
                    date = obj.optString("date", ""),
                    status = obj.optString("status", "Perlu Koreksi"),
                    note = obj.optString("note", "")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

private fun saveExperimentNotes(context: Context, notes: List<ExperimentNote>) {
    val prefs = context.getSharedPreferences("dipta_recipe_experiments", Context.MODE_PRIVATE)
    val array = JSONArray()
    notes.forEach { note ->
        val obj = JSONObject().apply {
            put("id", note.id)
            put("recipeTitle", note.recipeTitle)
            put("date", note.date)
            put("status", note.status)
            put("note", note.note)
        }
        array.put(obj)
    }
    prefs.edit().putString("notes_json", array.toString()).apply()
}

private fun List<Int>.indexOfMaxOrNull(): Int? {
    if (isEmpty()) return null
    var maxIndex = 0
    var maxValue = this[0]
    for (i in 1 until size) {
        if (this[i] > maxValue) {
            maxValue = this[i]
            maxIndex = i
        }
    }
    return maxIndex
}

@Composable
private fun TabStatistikContent(
    activeRecipes: List<Recipe>,
    mealPlanItems: List<MealPlanItem>,
    onRecipeClick: (Recipe) -> Unit
) {
    val context = LocalContext.current
    var experimentNotes by remember { mutableStateOf(loadExperimentNotes(context)) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Overview Banner (Ringkasan Statistik)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SageGreenPrimaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Statistik & Ringkasan Dapur",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SoftTextDark
                            )
                            Text(
                                text = "Evaluasi frekuensi memasak, rating penguasaan, & resep andalan",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricMiniCard(
                            label = "Resep Aktif",
                            value = "${activeRecipes.size}",
                            subtitle = "Tersimpan",
                            modifier = Modifier.weight(1f)
                        )
                        MetricMiniCard(
                            label = "Masak Mingguan",
                            value = "${mealPlanItems.size}",
                            subtitle = "Jadwal Menu",
                            modifier = Modifier.weight(1f)
                        )
                        MetricMiniCard(
                            label = "Proyeksi Bulanan",
                            value = "~${mealPlanItems.size * 4}",
                            subtitle = "Estimasi Porsi",
                            modifier = Modifier.weight(1f)
                        )
                        MetricMiniCard(
                            label = "Resep Juara",
                            value = "${activeRecipes.count { it.skillRating == 5 }}",
                            subtitle = "Rating 5★",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Grafik Frekuensi Memasak (Weekly Bar Chart)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Grafik Frekuensi Memasak Mingguan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SoftTextDark
                    )
                    Text(
                        text = "Jumlah jadwal menu masak per hari di Kalender Rencana",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val daysList = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
                    val dayShorts = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                    val dayCounts = daysList.map { day ->
                        mealPlanItems.count { it.dayOfWeek.equals(day, ignoreCase = true) }
                    }
                    val maxCount = dayCounts.maxOrNull()?.coerceAtLeast(1) ?: 1

                    // Custom Bar Chart Visualizer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dayCounts.forEachIndexed { idx, count ->
                            val heightRatio = count.toFloat() / maxCount.toFloat()
                            val barHeightDp = (heightRatio * 80).dp.coerceAtLeast(6.dp)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (count > 0) "${count}x" else "",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (count > 0) SageGreenPrimary else Color.Transparent
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height(85.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SoftCreamCanvas),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(barHeightDp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (count > 0) SageGreenPrimary else Color.LightGray.copy(alpha = 0.3f)
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = dayShorts[idx],
                                    fontSize = 11.sp,
                                    fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (count > 0) SoftTextDark else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val peakDayIndex = dayCounts.indexOfMaxOrNull()
                    val peakDayName = if (peakDayIndex != null && dayCounts[peakDayIndex] > 0) daysList[peakDayIndex] else null
                    val peakCount = if (peakDayIndex != null) dayCounts[peakDayIndex] else 0

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SageGreenPrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (peakDayName != null)
                                    "Hari Paling Sibuk Masak: $peakDayName ($peakCount menu dijadwalkan)"
                                else
                                    "Belum ada jadwal masak minggu ini. Sematkan resep di Kalender Rencana!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SageGreenPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. Menu Paling Sering Dibuat (Top Recipes)
        item {
            val topRecipes = remember(activeRecipes, mealPlanItems) {
                activeRecipes.map { recipe ->
                    val scheduledCount = mealPlanItems.count { it.recipeId == recipe.id || it.recipeTitle.equals(recipe.title, ignoreCase = true) }
                    val score = scheduledCount * 10 + recipe.skillRating * 3 + if (recipe.isFavorite) 5 else 0
                    Pair(recipe, scheduledCount) to score
                }
                .sortedByDescending { it.second }
                .take(5)
                .map { it.first }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏆 Menu Paling Sering Dibuat (Top Recipes)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SoftTextDark
                    )
                    Text(
                        text = "Peringkat resep favorit & paling sering dijadwalkan",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (topRecipes.isEmpty()) {
                        Text(
                            text = "Belum ada resep tersimpan di Buku Resep.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            topRecipes.forEachIndexed { rank, (recipe, scheduledCount) ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = SoftCreamCanvas),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onRecipeClick(recipe) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (rank == 0) Color(0xFFFFB300) else SageGreenPrimary,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "#${rank + 1}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = recipe.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = SoftTextDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (recipe.isFavorite) {
                                                    Icon(
                                                        imageVector = Icons.Default.Favorite,
                                                        contentDescription = null,
                                                        tint = Color(0xFFE91E63),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (recipe.category.isNotBlank()) {
                                                    Text(
                                                        text = recipe.category,
                                                        fontSize = 10.sp,
                                                        color = SageGreenPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = "Dijadwalkan: ${scheduledCount}x minggu ini",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        if (recipe.skillRating > 0) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFB300),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "${recipe.skillRating}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoftTextDark
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Lihat Detail",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Statistik Rating Penguasaan Resep (Bintang 1 - 5)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⭐ Tingkat Penguasaan Resep (Mastery)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SoftTextDark
                    )
                    Text(
                        text = "Proporsi kemahiran memasak Anda dari Bintang 1 hingga 5",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val total = activeRecipes.size.coerceAtLeast(1)
                    val star5Count = activeRecipes.count { it.skillRating == 5 }
                    val star4Count = activeRecipes.count { it.skillRating == 4 }
                    val star3Count = activeRecipes.count { it.skillRating == 3 }
                    val star2Count = activeRecipes.count { it.skillRating == 2 }
                    val star1Count = activeRecipes.count { it.skillRating == 1 }
                    val star0Count = activeRecipes.count { it.skillRating == 0 }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RatingBreakdownRow("★5 - Menu Juara Keluarga", star5Count, total, Color(0xFFFFB300))
                        RatingBreakdownRow("★4 - Sangat Mahir", star4Count, total, SageGreenPrimary)
                        RatingBreakdownRow("★3 - Lumayan Enak", star3Count, total, Color(0xFF42A5F5))
                        RatingBreakdownRow("★2 - Cukup Bisa", star2Count, total, Color(0xFFFFA726))
                        RatingBreakdownRow("★1 - Pemula / Belum Hafal", star1Count, total, Color(0xFFEF5350))
                        RatingBreakdownRow("Belum Dirating", star0Count, total, Color.Gray)
                    }
                }
            }
        }

        // 5. Log Eksperimen & Catatan Evaluasi ("Pernah Gagal/Koreksi")
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📝 Log Eksperimen & Catatan Evaluasi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SoftTextDark
                            )
                            Text(
                                text = "Riwayat evaluasi saat resep kurang pas, gagal, atau butuh koreksi",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { showAddNoteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_add_experiment_note")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Catatan Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (experimentNotes.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftCreamCanvas,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Belum Ada Catatan Evaluasi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = SoftTextDark
                                )
                                Text(
                                    text = "Catat bila rasa resep kurang pas atau gagal agar bisa dievaluasi & diperbaiki pada eksekusi berikutnya!",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            experimentNotes.forEach { noteItem ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = SoftCreamCanvas),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = when (noteItem.status) {
                                                        "Perlu Koreksi" -> Color(0xFFFFEBEE)
                                                        "Tips & Improvisasi" -> Color(0xFFE3F2FD)
                                                        else -> Color(0xFFE8F5E9)
                                                    }
                                                ) {
                                                    Text(
                                                        text = noteItem.status,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (noteItem.status) {
                                                            "Perlu Koreksi" -> Color(0xFFC62828)
                                                            "Tips & Improvisasi" -> Color(0xFF1565C0)
                                                            else -> Color(0xFF2E7D32)
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }

                                                Text(
                                                    text = noteItem.recipeTitle,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = SoftTextDark
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = noteItem.date,
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                                IconButton(
                                                    onClick = {
                                                        val updated = experimentNotes.filter { it.id != noteItem.id }
                                                        experimentNotes = updated
                                                        saveExperimentNotes(context, updated)
                                                        Toast.makeText(context, "Catatan dihapus", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Hapus Catatan",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = noteItem.note,
                                            fontSize = 12.sp,
                                            color = SoftTextDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Experiment Note Dialog
    if (showAddNoteDialog) {
        AddExperimentNoteDialog(
            activeRecipes = activeRecipes,
            onDismiss = { showAddNoteDialog = false },
            onSave = { recipeTitle, status, noteText ->
                val currentDateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
                val newNote = ExperimentNote(
                    recipeTitle = recipeTitle.ifBlank { "Resep Umum" },
                    date = currentDateStr,
                    status = status,
                    note = noteText
                )
                val updated = listOf(newNote) + experimentNotes
                experimentNotes = updated
                saveExperimentNotes(context, updated)
                showAddNoteDialog = false
                Toast.makeText(context, "Catatan evaluasi berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun MetricMiniCard(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SoftCreamCanvas,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = SageGreenPrimary
            )
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = SoftTextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun RatingBreakdownRow(
    label: String,
    count: Int,
    total: Int,
    barColor: Color
) {
    val progress = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = SoftTextDark,
            modifier = Modifier.width(140.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(SoftCreamCanvas)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor)
            )
        }

        Text(
            text = "$count resep",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun AddExperimentNoteDialog(
    activeRecipes: List<Recipe>,
    onDismiss: () -> Unit,
    onSave: (recipeTitle: String, status: String, note: String) -> Unit
) {
    var selectedRecipeTitle by remember { mutableStateOf(if (activeRecipes.isNotEmpty()) activeRecipes.first().title else "") }
    var selectedStatus by remember { mutableStateOf("Perlu Koreksi") }
    var noteText by remember { mutableStateOf("") }
    val statuses = listOf("Perlu Koreksi", "Tips & Improvisasi", "Eksperimen Sukses")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Catat Evaluasi / Koreksi Resep",
                fontWeight = FontWeight.Bold,
                color = SoftTextDark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Recipe selection or custom name
                Column {
                    Text("Pilih / Ketik Nama Resep:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (activeRecipes.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            items(activeRecipes) { recipe ->
                                val isSelected = selectedRecipeTitle.equals(recipe.title, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedRecipeTitle = recipe.title },
                                    label = { Text(recipe.title, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = selectedRecipeTitle,
                        onValueChange = { selectedRecipeTitle = it },
                        label = { Text("Nama Resep / Masakan") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_experiment_recipe_title"),
                        singleLine = true
                    )
                }

                // Status Tag Selector
                Column {
                    Text("Status Evaluasi:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        statuses.forEach { status ->
                            val isSel = selectedStatus == status
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedStatus = status },
                                label = { Text(status, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (status) {
                                        "Perlu Koreksi" -> Color(0xFFE53935)
                                        "Tips & Improvisasi" -> Color(0xFF1E88E5)
                                        else -> Color(0xFF43A047)
                                    },
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Note Text Field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Catatan Evaluasi / Koreksi Rasa") },
                    placeholder = { Text("mis. Bumbu halus perlu ditumis lebih lama, garam dikurangi 1 sdt.") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_experiment_note_text")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedRecipeTitle, selectedStatus, noteText) },
                enabled = noteText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Simpan Catatan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
