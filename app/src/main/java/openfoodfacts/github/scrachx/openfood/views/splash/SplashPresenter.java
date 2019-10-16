package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.LoadTaxonomiesService;

/**
 * Created by Lobster on 03.03.18.
 */
public class SplashPresenter implements ISplashPresenter.Actions {
    /**
     * Mutiplied by 6*30* to reduce the issue. TODO: fix https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/1616
     */
    private final Long REFRESH_PERIOD = 6 * 30 * 24 * 60 * 60 * 1000L;
    private ISplashPresenter.View view;
    private SharedPreferences settings;
    Context context;

    public SplashPresenter(SharedPreferences settings, ISplashPresenter.View view, Context context) {
        this.view = view;
        this.settings = settings;
        this.context = context;
    }

    @Override
    public void refreshData() {
        //Keep a trace of the last download date for the taxonomies.
        if(!settings.contains("lastDownloadcategories")){
            //If lastDownloadcategories is not set then it's the first time we run this new code
            //Set to 1 the lastDownload for the taxonomies to be loaded :
            //This one is needed for all flavors :
            settings.edit().putLong("lastDownloadcategories", Long.valueOf(ProductRepository.TAXONOMY_TO_BE_LOADED)).apply();
            //unless I'm mistaken, tags are generated by the others taxonomies but have no taxonomy file of its own
            //settings.edit().putLong("lastDownloadtags", Long.valueOf(ProductRepository.TAXONOMY_TO_BE_LOADED)).apply();
            if (BuildConfig.FLAVOR.equals("off") || BuildConfig.FLAVOR.equals("obf")) {
                //Additives, Countries, Labels and Ingredients are used here
                settings.edit().putLong("lastDownloadadditives", Long.valueOf(ProductRepository.TAXONOMY_TO_BE_LOADED)).apply();
                settings.edit().putLong("lastDownloadcountries", Long.valueOf(ProductRepository.TAXONOMY_TO_BE_LOADED)).apply();
                settings.edit().putLong("lastDownloadlabels", Long.valueOf(ProductRepository.TAXONOMY_TO_BE_LOADED)).apply();
                //For the moment, ingredients are only need if Diet is use.
                settings.edit().putLong("lastDownloadingredients", Long.valueOf(ProductRepository.TAXONOMY_NOT_TO_BE_LOADED)).apply();
            }
            if (BuildConfig.FLAVOR.equals("off")) {
                //OFF also needs Allergens.
                settings.edit().putLong("lastDownloadallergens", Long.valueOf(ProductRepository.TAXONOMY_TO_BE_LOADED)).apply();
            }
        }
        //first run ever off this application, whatever the version
        boolean firstRun = settings.getBoolean("firstRun", true);
        if (firstRun) {
            settings.edit()
                .putBoolean("firstRun", false)
                .apply();
        }
        if (isNeedToRefresh()) { //true if data was refreshed more than 1 day ago
            Intent intent = new Intent(context, LoadTaxonomiesService.class);
            context.startService(intent);
        }
        if (firstRun) {
            new Handler().postDelayed(() -> view.navigateToMainActivity(), 6000);
        } else {
            view.navigateToMainActivity();
        }
    }

    /*
     * This method checks if data was refreshed more than 1 day ago
     */
    private Boolean isNeedToRefresh() {
        return System.currentTimeMillis() - settings.getLong(Utils.LAST_REFRESH_DATE, 0) > REFRESH_PERIOD;
    }
}
