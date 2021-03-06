package openfoodfacts.github.scrachx.openfood.repositories;

import android.content.SharedPreferences;
import android.util.Log;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AdditiveNameDao;
import openfoodfacts.github.scrachx.openfood.models.AdditivesWrapper;
import openfoodfacts.github.scrachx.openfood.models.Allergen;
import openfoodfacts.github.scrachx.openfood.models.AllergenDao;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AllergenNameDao;
import openfoodfacts.github.scrachx.openfood.models.AllergensWrapper;
import openfoodfacts.github.scrachx.openfood.models.CategoriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.Category;
import openfoodfacts.github.scrachx.openfood.models.CategoryDao;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.CategoryNameDao;
import openfoodfacts.github.scrachx.openfood.models.CountriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.Country;
import openfoodfacts.github.scrachx.openfood.models.CountryDao;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.CountryNameDao;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.IngredientDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientName;
import openfoodfacts.github.scrachx.openfood.models.IngredientNameDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientsRelation;
import openfoodfacts.github.scrachx.openfood.models.IngredientsRelationDao;
import openfoodfacts.github.scrachx.openfood.models.IngredientsWrapper;
import openfoodfacts.github.scrachx.openfood.models.InsightAnnotationResponse;
import openfoodfacts.github.scrachx.openfood.models.Label;
import openfoodfacts.github.scrachx.openfood.models.LabelDao;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.LabelNameDao;
import openfoodfacts.github.scrachx.openfood.models.LabelsWrapper;
import openfoodfacts.github.scrachx.openfood.models.Question;
import openfoodfacts.github.scrachx.openfood.models.QuestionsState;
import openfoodfacts.github.scrachx.openfood.models.Tag;
import openfoodfacts.github.scrachx.openfood.models.TagDao;
import openfoodfacts.github.scrachx.openfood.models.TagsWrapper;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.network.ProductApiService;
import openfoodfacts.github.scrachx.openfood.network.RobotoffAPIService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

/**
 * This is a repository class which implements repository interface.
 * @author Lobster
 * @since 03.03.18
 */

public class ProductRepository implements IProductRepository {

    private static final String DEFAULT_LANGUAGE = "en";
    private static final String TAG= ProductRepository.class.getSimpleName();

    private static IProductRepository instance;

    private ProductApiService productApi;
    private OpenFoodAPIService openFooApi;
    private RobotoffAPIService robotoffApi;

    private Database db;
    private LabelDao labelDao;
    private LabelNameDao labelNameDao;
    private TagDao tagDao;
    private AllergenDao allergenDao;
    private AllergenNameDao allergenNameDao;
    private AdditiveDao additiveDao;
    private AdditiveNameDao additiveNameDao;
    private CountryDao countryDao;
    private CountryNameDao countryNameDao;
    private CategoryDao categoryDao;
    private CategoryNameDao categoryNameDao;
    private IngredientDao ingredientDao;
    private IngredientNameDao ingredientNameDao;
    private IngredientsRelationDao ingredientsRelationDao;

    // -1 no internet connexion.
    public final static Long TAXONOMY_NO_INTERNET = -1L;
    //  0 taxonomy is not marked to be load.
    public final static Long TAXONOMY_NOT_TO_BE_LOADED =0L;
    //  1 taxonomy has to be download.
    public final static Long TAXONOMY_TO_BE_LOADED = 1L;
    //  1 taxonomy is up to date.
    public final static Long TAXONOMY_IS_UP_TO_DATE = 2L;

    /**
     * A method used to get instance from the repository.
     * @return : instance of the repository
     */
    public static IProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }

        return instance;
    }

    /**
     * Constructor of the class which is used to initialize objects.
     */
    private ProductRepository() {
        productApi = CommonApiManager.getInstance().getProductApiService();
        openFooApi = CommonApiManager.getInstance().getOpenFoodApiService();
        robotoffApi = CommonApiManager.getInstance().getRobotoffApiService();

        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        db = daoSession.getDatabase();
        labelDao = daoSession.getLabelDao();
        labelNameDao = daoSession.getLabelNameDao();
        tagDao = daoSession.getTagDao();
        allergenDao = daoSession.getAllergenDao();
        allergenNameDao = daoSession.getAllergenNameDao();
        additiveDao = daoSession.getAdditiveDao();
        additiveNameDao = daoSession.getAdditiveNameDao();
        countryDao = daoSession.getCountryDao();
        countryNameDao = daoSession.getCountryNameDao();
        categoryDao = daoSession.getCategoryDao();
        categoryNameDao = daoSession.getCategoryNameDao();
        ingredientDao = daoSession.getIngredientDao();
        ingredientNameDao = daoSession.getIngredientNameDao();
        ingredientsRelationDao = daoSession.getIngredientsRelationDao();
    }

    /**
     * Load labels from the server or local database
     *
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *                If checkUpdate is true (or local database is empty) than load it from the server,
     *                else from the local database.
     * @return The list of Labels.
     */
    @Override
    public Single<List<Label>> getLabels(Boolean checkUpdate) {
        //First check if this taxonomy is to be loaded.
        String taxonomy = "labels";
        Long lastDownload = getLastDownload(taxonomy);
        if (lastDownload > 0) {
            //Taxonomy is marked to be download
            Long lastModifiedDate;
            if (tableIsEmpty(labelDao)) {
                //Table is empty, no check for update, just load taxonomy
                lastModifiedDate = getLastModifiedDate(taxonomy);
                return loadLabels(lastModifiedDate);
            } else if (checkUpdate) {
                //It is ask to check for update - Test if file on server is more recent than last download.
                lastModifiedDate = CheckUpdateSinceLastDownload(taxonomy);
                if (lastModifiedDate > TAXONOMY_IS_UP_TO_DATE) {
                    //Download taxonomy from server
                    return loadLabels(lastModifiedDate);
                }
            }
        }
        //If we are here then just get the information from the local database
        return Single.fromCallable(() -> labelDao.loadAll());
    }

    public Single<List<Label>> loadLabels(Long lastModifiedDate){
        return productApi.getLabels()
            .map(LabelsWrapper::map)
            .doOnSuccess(__ -> updateLastDownload("labels", lastModifiedDate));
    }

    /**
     * Load tags from the server or local database
     *
     * @param refresh defines the source of data.
     *                If refresh is true (or local database is empty) than load it from the server,
     *                else from the local database.
     * @return The list of Tags.
     */
    @Override
    public Single<List<Tag>> getTags(Boolean refresh) {
        if (refresh || tableIsEmpty(labelDao)) {
            return openFooApi.getTags()
                    .map(TagsWrapper::getTags);
        } else {
            return Single.fromCallable(() -> tagDao.loadAll());
        }
    }

    /**
     * Load allergens from the server or local database
     *
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *                If checkUpdate is true (or local database is empty) than load it from the server,
     *                else from the local database.
     * @return The allergens in the product.
     */
    @Override
    public Single<List<Allergen>> getAllergens(Boolean checkUpdate) {
        //First check if this taxonomy is to be loaded.
        String taxonomy = "allergens";
        Long lastDownload = getLastDownload(taxonomy);
        if (lastDownload > 0) {
            //Taxonomy is marked to be download
            Long lastModifiedDate;
            if (tableIsEmpty(allergenDao)) {
                //Table is empty, no check for update, just load taxonomy
                lastModifiedDate = getLastModifiedDate(taxonomy);
                return loadAllergens(lastModifiedDate);
            } else if (checkUpdate) {
                //It is ask to check for update - Test if file on server is more recent than last download.
                lastModifiedDate = CheckUpdateSinceLastDownload(taxonomy);
                if (lastModifiedDate > TAXONOMY_IS_UP_TO_DATE) {
                    //Download taxonomy from server
                    return loadAllergens(lastModifiedDate);
                }
            }
        }
        //If we are here then just get the information from the local database
        return Single.fromCallable(() -> allergenDao.loadAll());
    }

    public Single<List<Allergen>> loadAllergens(Long lastModifiedDate){
            return productApi.getAllergens()
                .map(AllergensWrapper::map)
                .doOnSuccess(__ -> updateLastDownload("allergens", lastModifiedDate));
    }

    /**
     * Load countries from the server or local database
     *
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *                If checkUpdate is true (or local database is empty) than load it from the server,
     *                else from the local database.
     * @return The list of countries.
     */
    @Override
    public Single<List<Country>> getCountries(Boolean checkUpdate) {
        //First check if this taxonomy is to be loaded.
        String taxonomy = "countries";
        Long lastDownload = getLastDownload(taxonomy);
        if (lastDownload > 0) {
            //Taxonomy is marked to be download
            Long lastModifiedDate;
            if (tableIsEmpty(countryDao)) {
                //Table is empty, no check for update, just load taxonomy
                lastModifiedDate = getLastModifiedDate(taxonomy);
                return loadCountries(lastModifiedDate);
            } else if (checkUpdate) {
                //It is ask to check for update - Test if file on server is more recent than last download.
                lastModifiedDate = CheckUpdateSinceLastDownload(taxonomy);
                if (lastModifiedDate > TAXONOMY_IS_UP_TO_DATE) {
                    //Download taxonomy from server
                    return loadCountries(lastModifiedDate);
                }
            }
        }
        //If we are here then just get the information from the local database
        return Single.fromCallable(() -> countryDao.loadAll());
    }

    public Single<List<Country>> loadCountries(Long lastModifiedDate){
        return productApi.getCountries()
            .map(CountriesWrapper::map)
            .doOnSuccess(__ -> updateLastDownload("countries", lastModifiedDate));
    }

    /**
     * Load categories from the server or local database
     *
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *                If checkUpdate is true (or local database is empty) than load it from the server,
     *                else from the local database.
     * @return The list of categories.
     */
    @Override
    public Single<List<Category>> getCategories(Boolean checkUpdate) {
        //First check if this taxonomy is to be loaded.
        String taxonomy = "categories";
        Long lastDownload = getLastDownload(taxonomy);
        if (lastDownload > 0) {
            //Taxonomy is marked to be download
            Long lastModifiedDate;
            if (tableIsEmpty(categoryDao)) {
                //Table is empty, no check for update, just load taxonomy
                lastModifiedDate = getLastModifiedDate(taxonomy);
                return loadCategories(lastModifiedDate);
            } else if (checkUpdate) {
                //It is ask to check for update - Test if file on server is more recent than last download.
                lastModifiedDate = CheckUpdateSinceLastDownload(taxonomy);
                if (lastModifiedDate > TAXONOMY_IS_UP_TO_DATE) {
                    //Download taxonomy from server
                    return loadCategories(lastModifiedDate);
                }
            }
        }
        //If we are here then just get the information from the local database
        return Single.fromCallable(() -> categoryDao.loadAll());
    }

    public Single<List<Category>> loadCategories(Long lastModifiedDate){
        return productApi.getCategories()
            .map(CategoriesWrapper::map)
            .doOnSuccess(__ -> updateLastDownload("categories", lastModifiedDate));
    }

    /**
     * Load allergens which user selected earlier (i.e user's allergens)
     * @return The list of allergens.
     */
    @Override
    public List<Allergen> getEnabledAllergens() {
        return allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq("true")).list();
    }

    /**
     * Load additives from the server or local database
     *
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *                If checkUpdate is true (or local database is empty) than load it from the server,
     *                else from the local database.
     * @return The list of additives.
     */
    @Override
    public Single<List<Additive>> getAdditives(Boolean checkUpdate) {
        //First check if this taxonomy is to be loaded.
        String taxonomy = "additives";
        Long lastDownload = getLastDownload(taxonomy);
        if (lastDownload > 0) {
            //Taxonomy is marked to be download
            Long lastModifiedDate;
            if (tableIsEmpty(additiveDao)) {
                //Table is empty, no check for update, just load taxonomy
                lastModifiedDate = getLastModifiedDate(taxonomy);
                return loadAdditives(lastModifiedDate);
            } else if (checkUpdate) {
                //It is ask to check for update - Test if file on server is more recent than last download.
                lastModifiedDate = CheckUpdateSinceLastDownload(taxonomy);
                if (lastModifiedDate > TAXONOMY_IS_UP_TO_DATE) {
                    //Download taxonomy from server
                    return loadAdditives(lastModifiedDate);
                }
            }
        }
        //If we are here then just get the information from the local database
        return Single.fromCallable(() -> additiveDao.loadAll());
    }

    public Single<List<Additive>> loadAdditives(Long lastModifiedDate){
        return productApi.getAdditives()
            .map(AdditivesWrapper::map)
            .doOnSuccess(__ -> updateLastDownload("additives", lastModifiedDate));
    }

    /**
     * TODO to be improved by loading only in the user language ?
     * Load ingredients from (the server or) local database
     * If SharedPreferences lastDownloadIngredients is set try this :
     *  if file from the server is newer than last download delete database, load the file and fill database,
     *  else if database is empty, download the file and fill database,
     *  else return the content from the local database.
     *
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *                If checkUpdate is true (or local database is empty) than load it from the server,
     *
     * @return The ingredients in the product.
     */
    @Override
    public Single<List<Ingredient>> getIngredients(Boolean checkUpdate) {
        //First check if this taxonomy is to be loaded.
        String taxonomy = "ingredients";
        Long lastDownload = getLastDownload(taxonomy);
        if (lastDownload > 0) {
            //Taxonomy is marked to be download
            Long lastModifiedDate;
            if (tableIsEmpty(ingredientDao)) {
                //Table is empty, no check for update, just load taxonomy
                lastModifiedDate = getLastModifiedDate(taxonomy);
                return loadIngredients(lastModifiedDate);
            } else if (checkUpdate) {
                //It is ask to check for update - Test if file on server is more recent than last download.
                lastModifiedDate = CheckUpdateSinceLastDownload(taxonomy);
                if (lastModifiedDate > TAXONOMY_IS_UP_TO_DATE) {
                    //Download taxonomy from server
                    return loadIngredients(lastModifiedDate);
                }
            }
        }
        //If we are here then just get the information from the local database
        return Single.fromCallable(() -> ingredientDao.loadAll());
    }

    public Single<List<Ingredient>> loadIngredients(Long lastModifiedDate){
        return productApi.getIngredients()
            .map(IngredientsWrapper::map)
            .doOnSuccess(__ -> updateLastDownload("ingredients", lastModifiedDate));
    }

    /**
     * This function check the last modified date of the taxonomy.json file on OF server.
     *
     * @param taxonomy              The lowercase taxonomy to be check
     *
     * @return lastModifierDate     The timestamp of the last changes date of the taxonomy.json on OF server
     *                              Or TAXONOMY_NO_INTERNET if there is no connexion.
     */
    public Long getLastModifiedDate(String taxonomy) {
        long lastModifiedDate = 0;
        try {
            URL url = new URL(BuildConfig.OFWEBSITE + "data/taxonomies/" + taxonomy + ".json");
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            lastModifiedDate = httpCon.getLastModified();
            httpCon.disconnect();
        } catch (IOException e) {
            //Problem
            e.printStackTrace();
            Log.i("INFO_URL", "getLastModifiedDate for : " + taxonomy + " end, return " + TAXONOMY_NO_INTERNET);
            return  TAXONOMY_NO_INTERNET;
        }
        Log.i("INFO_URL", "getLastModifiedDate for : " + taxonomy + " end, return " + lastModifiedDate);
        return lastModifiedDate;
    }

    /**
     * This function test if a taxonomy needs to be uploaded
     *
     * @param taxonomy The name of the taxonomy to be tested
     *        (allergens, additives, categories, countries, ingredients, labels, tags)
     *
     * @return
     *     TAXONOMY_NO_INTERNET (-1)        no internet connexion.
     *     TAXONOMY_NOT_TO_BE_LOADED (0)    taxonomy is not marked to be load.
     *     TAXONOMY_IS_UP_TO_DATE (2)       taxonomy is up to date.
     *     other :                          date of the new taxonomy on the servers => to be updated
     *
     *     Note that TAXONOMY_TO_BE_LOADED (1) is just use to ask taxonomy to be loaded and never returned by this method.
     */
    public Long CheckUpdateSinceLastDownload(String taxonomy) {
        Log.i("INFO_URL", "CheckUpdateSinceLastDownload for : " + taxonomy + " begin.");
        Long lastDownload = getLastDownload(taxonomy);
        if (lastDownload == TAXONOMY_TO_BE_LOADED) {
            //This taxonomy has to be loaded, no test is needed, just return currentTimestamp().
            return System.currentTimeMillis();
        } else if (lastDownload > TAXONOMY_TO_BE_LOADED) {
            //In that case we must download this taxonomy .json unless we already downloaded the latest version.
            //Get Last modified date for the file on sever.
            long lastModifiedDate = getLastModifiedDate(taxonomy);
            if (lastModifiedDate > lastDownload) {
                //File on server is more recent that last download.
                Log.i("INFO_URL", "CheckUpdateSinceLastDownload for : " + taxonomy + " end, return " + lastModifiedDate);
                return lastModifiedDate;
            } else {
                //File on server has not change since last download
                Log.i("INFO_URL", "CheckUpdateSinceLastDownload for : " + taxonomy + " end, return " + TAXONOMY_IS_UP_TO_DATE);
                return TAXONOMY_IS_UP_TO_DATE;
            }
        }
        //Well, the file is not marked to be loaded.
        Log.i("INFO_URL", "CheckUpdateSinceLastDownload for : " + taxonomy + " end, return " + TAXONOMY_NOT_TO_BE_LOADED);
        return TAXONOMY_NOT_TO_BE_LOADED;
    }

    /**
     * This function set lastDownloadtaxonomy setting
     * @param taxonomy  Name of the taxonomy (allergens, additives, categories, countries, ingredients, labels, tags)
     * @param lastDownload    Date of last update on Long format
     */
    public void updateLastDownload(String taxonomy, Long lastDownload){
        SharedPreferences mSettings = OFFApplication.getInstance().getSharedPreferences("prefs", 0);
        mSettings.edit().putLong("lastDownload" + taxonomy, lastDownload).apply();
        Log.i("INFO_URL", "Set lastDownload of " + taxonomy + " to " + lastDownload);
    }

    /**
     * This function get lastDownloadtaxonomy setting
     * @param taxonomy  Name of the taxonomy (allergens, additives, categories, countries, ingredients, labels, tags)
     *
     * @return Actual value of lastDownloadtaxonomy in the setting.
     */
    public Long getLastDownload(String taxonomy){
        SharedPreferences mSettings = OFFApplication.getInstance().getSharedPreferences("prefs", 0);
        Long lastDownload = mSettings.getLong("lastDownload" + taxonomy, TAXONOMY_NOT_TO_BE_LOADED);
        Log.i("INFO_URL", "getLastDownload of " + taxonomy + " is " + lastDownload);
        return lastDownload;
    }

    /**
     * Labels saving to local database
     * @param labels The list of labels to be saved.
     * <p>
     * Label and LabelName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveLabels(List<Label> labels) {
        db.beginTransaction();
        try {
            for (Label label : labels) {
                labelDao.insertOrReplace(label);
                for (LabelName labelName : label.getNames()) {
                    labelNameDao.insertOrReplace(labelName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG,"saveLabels",e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Tags saving to local database
     * @param tags The list of tags to be saved.
     */
    @Override
    public void saveTags(List<Tag> tags) {
        tagDao.insertOrReplaceInTx(tags);
    }


    /**
     * Allergens saving to local database
     * @param allergens The list of allergens to be saved.
     * <p>
     * Allergen and AllergenName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveAllergens(List<Allergen> allergens) {
        db.beginTransaction();
        try {
            for (Allergen allergen : allergens) {
                allergenDao.insertOrReplace(allergen);
                for (AllergenName allergenName : allergen.getNames()) {
                    allergenNameDao.insertOrReplace(allergenName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG,"saveAllergens",e);
        } finally {
            db.endTransaction();
        }
    }


    /**
     * Additives saving to local database
     * @param additives The list of additives to be saved.
     * <p>
     * Additive and AdditiveName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveAdditives(List<Additive> additives) {
        db.beginTransaction();
        try {
            for (Additive additive : additives) {
                additiveDao.insertOrReplace(additive);
                for (AdditiveName allergenName : additive.getNames()) {
                    additiveNameDao.insertOrReplace(allergenName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG,"saveAdditives",e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Countries saving to local database
     * @param countries The list of countries to be saved.
     * <p>
     * Country and CountryName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveCountries(List<Country> countries) {
        db.beginTransaction();
        try {
            for (Country country : countries) {
                countryDao.insertOrReplace(country);
                for (CountryName countryName : country.getNames()) {
                    countryNameDao.insertOrReplace(countryName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG,"saveCountries",e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Categories saving to local database
     * @param categories The list of categories to be saved.
     * <p>
     * Category and CategoryName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveCategories(List<Category> categories) {
        db.beginTransaction();
        try {
            for (Category category : categories) {
                categoryDao.insertOrReplace(category);
                for (CategoryName categoryName : category.getNames()) {
                    categoryNameDao.insertOrReplace(categoryName);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG,"saveCategories",e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Delete rows from Ingredient, IngredientName and IngredientsRelation
     * set the autoincrement to 0
     */
    @Override
    public void deleteIngredientCascade(){
        ingredientDao.deleteAll();
        ingredientNameDao.deleteAll();
        ingredientsRelationDao.deleteAll();
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        daoSession.getDatabase().execSQL("update sqlite_sequence set seq=0 where name in ('" + ingredientDao.getTablename() + "', '" + ingredientNameDao.getTablename() + "', '" + ingredientsRelationDao.getTablename() + "')");
    }

    /**
     * TODO to be improved by loading only if required and only in the user language
     * Ingredients saving to local database
     * @param ingredients The list of ingredients to be saved.
     * <p>
     * Ingredient and IngredientName has One-To-Many relationship, therefore we need to save them separately.
     */
    @Override
    public void saveIngredients(List<Ingredient> ingredients) {
        db.beginTransaction();
        try {
            for (Ingredient ingredient : ingredients) {
                ingredientDao.insertOrReplace(ingredient);
                for (IngredientName ingredientName : ingredient.getNames()) {
                    ingredientNameDao.insertOrReplace(ingredientName);
                }
                for (IngredientsRelation ingredientsRelation : ingredient.getParents()) {
                    ingredientsRelationDao.insertOrReplace(ingredientsRelation);
                }
                for (IngredientsRelation ingredientsRelation : ingredient.getChildren()) {
                    ingredientsRelationDao.insertOrReplace(ingredientsRelation);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG,"saveIngredients",e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Ingredient saving to local database
     * @param ingredient The ingredient to be saved.
     */
    @Override
    public void saveIngredient(Ingredient ingredient) {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        saveIngredients(ingredients);
    }

    /**
     * Changes enabled field of allergen and updates it.
     *
     * @param isEnabled   depends on whether user selected or unselected the allergen
     * @param allergenTag is unique Id of allergen
     */
    @Override
    public void setAllergenEnabled(String allergenTag, Boolean isEnabled) {
        Allergen allergen = allergenDao.queryBuilder()
                .where(AllergenDao.Properties.Tag.eq(allergenTag))
                .unique();

        if (allergen != null) {
            allergen.setEnabled(isEnabled);
            allergenDao.update(allergen);
        }
    }


    /**
     * Loads translated label from the local database by unique tag of label and language code
     *
     * @param labelTag     is a unique Id of label
     * @param languageCode is a 2-digit language code
     * @return The translated label
     */
    @Override
    public Single<LabelName> getLabelByTagAndLanguageCode(String labelTag, String languageCode) {
        return Single.fromCallable(() -> {
            LabelName labelName = labelNameDao.queryBuilder()
                    .where(
                            LabelNameDao.Properties.LabelTag.eq(labelTag),
                            LabelNameDao.Properties.LanguageCode.eq(languageCode)
                    ).unique();

            return labelName != null ? labelName : new LabelName();
        });
    }

    /**
     * Loads translated label from the local database by unique tag of label and default language code
     *
     * @param labelTag is a unique Id of label
     * @return The translated label
     */
    @Override
    public Single<LabelName> getLabelByTagAndDefaultLanguageCode(String labelTag) {
        return getLabelByTagAndLanguageCode(labelTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and language code
     *
     * @param additiveTag  is a unique Id of additive
     * @param languageCode is a 2-digit language code
     * @return The translated additive name
     */
    @Override
    public Single<AdditiveName> getAdditiveByTagAndLanguageCode(String additiveTag, String languageCode) {
        return Single.fromCallable(() -> {
            AdditiveName additiveName = additiveNameDao.queryBuilder()
                    .where(
                            AdditiveNameDao.Properties.AdditiveTag.eq(additiveTag),
                            AdditiveNameDao.Properties.LanguageCode.eq(languageCode)
                    ).unique();

            return additiveName != null ? additiveName : new AdditiveName();
        });
    }

    /**
     * Loads translated additive from the local database by unique tag of additive and default language code
     *
     * @param additiveTag is a unique Id of additive
     * @return The translated additive tag
     */
    @Override
    public Single<AdditiveName> getAdditiveByTagAndDefaultLanguageCode(String additiveTag) {
        return getAdditiveByTagAndLanguageCode(additiveTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated country from the local database by unique tag of country and language code
     *
     * @param countryTag   is a unique Id of country
     * @param languageCode is a 2-digit language code
     * @return The translated country name
     */
    @Override
    public Single<CountryName> getCountryByTagAndLanguageCode(String countryTag, String languageCode) {
        return Single.fromCallable(() -> {
            CountryName countryName = countryNameDao.queryBuilder()
                    .where(
                            CountryNameDao.Properties.CountyTag.eq(countryTag),
                            CountryNameDao.Properties.LanguageCode.eq(languageCode)
                    ).unique();

            return countryName != null ? countryName : new CountryName();
        });
    }

    /**
     * Loads translated country from the local database by unique tag of country and default language code
     *
     * @param countryTag is a unique Id of country
     * @return The translated country name
     */
    @Override
    public Single<CountryName> getCountryByTagAndDefaultLanguageCode(String countryTag) {
        return getCountryByTagAndLanguageCode(countryTag, DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated category from the local database by unique tag of category and language code
     *
     * @param categoryTag  is a unique Id of category
     * @param languageCode is a 2-digit language code
     * @return The translated category name
     */
    @Override
    public Single<CategoryName> getCategoryByTagAndLanguageCode(String categoryTag, String languageCode) {
        return Single.fromCallable(() -> {
            CategoryName categoryName = categoryNameDao.queryBuilder()
                    .where(
                            CategoryNameDao.Properties.CategoryTag.eq(categoryTag),
                            CategoryNameDao.Properties.LanguageCode.eq(languageCode)
                    ).unique();

            if (categoryName != null) {
                return categoryName;
            } else {
                CategoryName emptyCategoryName = new CategoryName();
                emptyCategoryName.setName(categoryTag);
                emptyCategoryName.setCategoryTag(categoryTag);
                emptyCategoryName.setIsWikiDataIdPresent(false);
                return emptyCategoryName;
            }
        });
    }

    /**
     * Loads translated category from the local database by unique tag of category and default language code
     *
     * @param categoryTag is a unique Id of category
     * @return The translated category name
     */
    @Override
    public Single<CategoryName> getCategoryByTagAndDefaultLanguageCode(String categoryTag) {
        return getCategoryByTagAndLanguageCode(categoryTag, DEFAULT_LANGUAGE);
    }



    /**
     * Loads list of translated category names from the local database by language code
     *
     * @param languageCode is a 2-digit language code
     * @return The translated list of category name
     */
    @Override
    public Single<List<CategoryName>> getAllCategoriesByLanguageCode(String languageCode) {
        return Single.fromCallable(() -> categoryNameDao.queryBuilder()
                .where(CategoryNameDao.Properties.LanguageCode.eq(languageCode))
                .orderAsc(CategoryNameDao.Properties.Name)
                .list());
    }

    /**
     * Loads list of category names from the local database by default language code
     *
     * @return The list of category name
     */
    @Override
    public Single<List<CategoryName>> getAllCategoriesByDefaultLanguageCode() {
        return getAllCategoriesByLanguageCode(DEFAULT_LANGUAGE);
    }

    /**
     * Loads translated and selected/unselected allergens.
     *
     * @param isEnabled    depends on whether allergen was selected or unselected by user
     * @param languageCode is a 2-digit language code
     * @return The list of allergen names
     */
    @Override
    public Single<List<AllergenName>> getAllergensByEnabledAndLanguageCode(Boolean isEnabled, String languageCode) {
        return Single.fromCallable(() -> {
            List<Allergen> allergens = allergenDao.queryBuilder().where(AllergenDao.Properties.Enabled.eq(isEnabled)).list();
            if (allergens != null) {
                List<AllergenName> allergenNames = new ArrayList<>();
                for (Allergen allergen : allergens) {
                    AllergenName name = allergenNameDao.queryBuilder()
                            .where(
                                    AllergenNameDao.Properties.AllergenTag.eq(allergen.getTag()),
                                    AllergenNameDao.Properties.LanguageCode.eq(languageCode)
                            ).unique();

                    if (name != null) {
                        allergenNames.add(name);
                    }
                }

                return allergenNames;
            }

            return new ArrayList<>();
        });
    }

    /**
     * Loads all translated allergens.
     *
     * @param languageCode is a 2-digit language code
     * @return The list of translated allergen names
     */
    @Override
    public Single<List<AllergenName>> getAllergensByLanguageCode(String languageCode) {
        return Single.fromCallable(() ->
                allergenNameDao.queryBuilder()
                        .where(AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                        .list());
    }

    /**
     * Loads translated allergen from the local database by unique tag of allergen and language code
     *
     * @param allergenTag  is a unique Id of allergen
     * @param languageCode is a 2-digit language code
     * @return The translated allergen name
     */
    @Override
    public Single<AllergenName> getAllergenByTagAndLanguageCode(String allergenTag, String languageCode) {
        return Single.fromCallable(() -> {
            AllergenName allergenName = allergenNameDao.queryBuilder()
                                                       .where(AllergenNameDao.Properties.AllergenTag.eq(allergenTag),
                                                              AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                                                       .unique();

            if (allergenName != null) {
                return allergenName;
            } else {
                AllergenName emptyAllergenName = new AllergenName();
                emptyAllergenName.setName(allergenTag);
                emptyAllergenName.setAllergenTag(allergenTag);
                emptyAllergenName.setIsWikiDataIdPresent(false);
                return emptyAllergenName;
            }
        });
    }

    /**
     * Loads translated allergen from the local database by unique tag of allergen and default language code
     *
     * @param allergenTag is a unique Id of allergen
     * @return The translated allergen name
     */
    @Override
    public Single<AllergenName> getAllergenByTagAndDefaultLanguageCode(String allergenTag) {
        return getAllergenByTagAndLanguageCode(allergenTag, DEFAULT_LANGUAGE);
    }

    /**
     * Checks whether table is empty
     *
     * @param dao checks records count of any table
     */
    private Boolean tableIsEmpty(AbstractDao dao) {
        return dao.count() == 0;
    }


    /**
     * Checks whether table of additives is empty
     */
    @Override
    public Boolean additivesIsEmpty() {
        return tableIsEmpty(additiveDao);
    }


    /**
     * Loads question from the local database by code and lang of question.
     *
     * @param code for the question
     * @param lang is language of the question
     * @return The single question
     */
    @Override
    public Single<Question> getSingleProductQuestion(String code, String lang) {
        return robotoffApi.getProductQuestion(code, lang, 1)
                .map(QuestionsState::getQuestions)
                .map(questions -> {
                    if (!questions.isEmpty()) {
                        return questions.get(0);
                    }
                    return QuestionsState.EMPTY_QUESTION;
                });
    }

    /**
     * Annotate the insight response using insight id and annotation
     * @param insightId is the unique id for the insight
     * @param annotation is the annotation to be used
     * @return The annotated insight response
     */
    @Override
    public Single<InsightAnnotationResponse> annotateInsight(String insightId, int annotation) {
        return robotoffApi.annotateInsight(insightId, annotation);
    }
}
