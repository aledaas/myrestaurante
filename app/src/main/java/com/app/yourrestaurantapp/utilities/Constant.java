package com.app.yourrestaurantapp.utilities;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.models.Category;
import com.app.yourrestaurantapp.models.Product;
import com.app.yourrestaurantapp.models.Slider;

import java.util.ArrayList;
import java.util.List;

public class Constant {

    //API Transactions
    private static final String BASE_URL = Config.ADMIN_PANEL_URL;
    public static final String GET_HOME = BASE_URL + "/api/api.php?get_home";
    public static final String GET_PRODUCT_ID = BASE_URL + "/api/api.php?product_id=";
    public static final String GET_CATEGORY = BASE_URL + "/api/api.php?get_category";
    public static final String GET_POST = BASE_URL + "/api/api.php?get_posts";
    public static final String GET_CATEGORY_DETAIL = BASE_URL + "/api/api.php?category_id=";
    public static final String GET_HELP = BASE_URL + "/api/api.php?get_help";
    public static final String GET_SETTINGS = BASE_URL + "/api/api.php?get_settings";
    public static final String GET_SHIPPING = BASE_URL + "/api/api.php?get_shipping";
    public static final String GET_RESTOS = BASE_URL + "/api/api.php?get_restos";
    public static final String POST_ORDER = BASE_URL + "/api/api.php?post_order";

    public static List<Slider> arrayListSlider = new ArrayList<>();
    public static List<Category> arrayListCategory = new ArrayList<>();
    public static List<Product> arrayListProduct = new ArrayList<>();

}
