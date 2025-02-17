package data.user;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import data.ErrorLogger;
import data.ReadSaveException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.util.*;

/**
 * @author benburnett
 * User class is an object that holds all of the information for a player throughout the game
 */
public class User {

    private static final String filePath = "resources/data/users.json";

    private boolean warned = false;

    private JSONObject json;
    private SaveUser save = new SaveUser();
    private UserHolder original;
    private Integer type;
    private String id;
    private String password;
    private String avatarImg;
    private List<Integer> birthday = new ArrayList<>();
    private int score;
    private Map<Integer, Integer> levelScores = new HashMap<>();
    private Map<Integer, List<Double>> levelPaths = new HashMap<>();

    /**
     * Constructor for existing account
     * Scans the user file for account information and instantiates a player object if it finds that info
     * @param id - the players username
     * @param password - the player's password
     * @throws InvalidLoginException - thrown if the id doesn't have an associated account or the password doesn't match
     * the username
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     */
    public User(String id, String password) throws InvalidLoginException {
        this.id = id;
        this.password = password;
        JSONParser jsonParser = new JSONParser();
        JSONObject users = new JSONObject();
        try {
            FileReader reader = new FileReader(filePath);
            users = (JSONObject)jsonParser.parse(reader);
        }
        catch (Exception e) {
            ErrorLogger.log(new ReadSaveException("read", filePath));
        }
        if (!users.keySet().contains(id)) {
            ErrorLogger.log(new InvalidLoginException(id));
        }
        JSONObject user = (JSONObject) users.get(id);

        if (user.get("password").equals(password)) unlockUser(user);
        else {
            InvalidLoginException e = new InvalidLoginException(id, password);
            ErrorLogger.log(e);
            throw e;
        }
    }

    /**
     * Constructor for a new account
     * Adds the new account to the user file and instanitates a player object for the new account
     * @param id - the player's username
     * @param password - the player's password
     * @param avatarImg - a string for the file path to the player's desired avatar
     * @param birthdayArray - a string array for the player's birthday
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     * @throws DuplicateUsernameException - thrown if the desired player id already exists
     */
    public User(String id, String password, String avatarImg, String[] birthdayArray) throws DuplicateUsernameException {
        this(id, password, avatarImg, Integer.parseInt(birthdayArray[0]), Integer.parseInt(birthdayArray[1]), Integer.parseInt(birthdayArray[2]));
    }

    /**
     * Constructor for a new account
     * Adds the new account to the user file and instanitates a player object for the new account
     * @param id - the player's username
     * @param password - the player's password
     * @param avatarImg - a string for the file path to the player's desired avatar
     * @param month - the month the player was born
     * @param day - the day the player was born
     * @param year - the year the player was born
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     * @throws DuplicateUsernameException - thrown if the desired player id already exists
     */
    public User(String id, String password, String avatarImg, int month, int day, int year) throws DuplicateUsernameException {
        JSONObject users = new JSONObject();
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(filePath);
            users = (JSONObject)jsonParser.parse(reader);
        }
        catch (Exception e) {
            ErrorLogger.log(new ReadSaveException("read", filePath));
        }
        if (users.keySet().contains(id)) throw new DuplicateUsernameException(id);
        this.id = id;
        this.password = password;
        this.avatarImg = avatarImg;
        birthday.addAll(Arrays.asList(month, day, year));
        score = 0;
        json = new JSONObject();
        json.put("id", id);
        json.put("password", password);
        json.put("avatar", avatarImg);
        JSONObject bday = new JSONObject();
        bday.put("month", birthday.get(0));
        bday.put("day", birthday.get(1));
        bday.put("year", birthday.get(2));
        json.put("birthday", bday);
        assignDefaults();
    }

    /**
     * Used is the constructors for setting up a new player
     * Instatiates all of the default instance variables for a new player
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     */
    private void assignDefaults() {
        json.put("score", 0);
        json.put("type", 1);
        levelScores.put(1, 0);
        JSONObject levelsJSON = new JSONObject();
        levelsJSON.put("1", 0);
        json.put("levels", levelsJSON);
        JSONObject pathsJSON = new JSONObject();
        pathsJSON.put("1", new ArrayList<>(Arrays.asList(0.0, 0.0)));
        json.put("paths", pathsJSON);
        levelPaths.put(1, new ArrayList<>(Arrays.asList(0.0, 0.0)));
        original = new UserHolder(json, id, password, avatarImg, birthday, score, levelScores, levelPaths);
        saveHelper();
    }

    /**
     * Reads a json file to retrieve all of the information from the last time the account was saved
     * @param data - a JSONObject containing all of the infomation on the account from the file
     */
    private void unlockUser(JSONObject data) {
        json = data;
        JSONObject birthdate = (JSONObject) data.get("birthday");
        birthday = new ArrayList<>(Arrays.asList(Optional.of(Math.toIntExact((Long) birthdate.get("month"))).orElse(0),
                Optional.of(Math.toIntExact((Long) birthdate.get("day"))).orElse(0),
                Optional.of(Math.toIntExact((Long) birthdate.get("year"))).orElse(0)));
        avatarImg = Optional.of((String)data.get("avatar")).orElse("");
        score = Optional.of(Math.toIntExact((Long)data.get("score"))).orElse(0);
        JSONObject levelScoresJSON = (JSONObject) data.get("levels");
        for (Object keyObj : levelScoresJSON.keySet()) {
            levelScores.put(Optional.of(Integer.parseInt((String) keyObj)).orElse(0),
                    Optional.of(Math.toIntExact((Long)levelScoresJSON.get(keyObj))).orElse(0));
        }
        type = Math.toIntExact((Long)json.get("type"));
        JSONObject pathsJSON = (JSONObject) data.get("paths");
        for (Object keyObj : pathsJSON.keySet()) {
            List<Double> path = new ArrayList<>();
            JSONArray pathJSON = (JSONArray) pathsJSON.get(keyObj);
            for (Object pointObj : pathJSON) {
                path.add(Optional.of((double)pointObj).orElse(0.0));
            }
            levelPaths.put(Optional.of(Integer.parseInt((String) keyObj)).orElse(0), path);
        }
        original = new UserHolder(json, id, password, avatarImg, birthday, score, levelScores, levelPaths);
    }

    /**
     * Passes the JSONObject that is keeping track of all of the changes to instance variables to the SaveUser class
     * where it can be saved
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     */
    private void saveHelper() {
        save.save(json);
    }

    /**
     * Changes the value of the player's score
     * @param change - the change in score
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     */
    public void updateScore(int change) {
        scoreHelper(score + change);
    }

    /**
     * Sets the player's score to the parameter
     * @param newScore - the player's new score
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     */
    public void replaceScore(int newScore) {
        scoreHelper(newScore);
    }

    /**
     * Helper method for updateScore() and replaceScore() - updates the score instance variable and the json file
     * @param newScore - the new score
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     */
    private void scoreHelper(int newScore) {
        score = Math.max(newScore, 0);
        json.replace("score", score);
        saveHelper();
    }

    /**
     * Updates the filepath to the player's chosen avatar
     * @param avatarImg - the new filepath
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     */
    public void changeAvatar(String avatarImg) {
        this.avatarImg = avatarImg;
        json.replace("avatar", avatarImg);
        saveHelper();
    }

    /**
     * Adds or replaces the player's high score for a certain level
     * @param level - the level the score was acheived on
     * @param newScore - the new score
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     */
    public void updateLevelScore(int level, int newScore) {
        if (level < 0) return;
        JSONObject levelsJSON = (JSONObject) json.get("levels");
        if (levelScores.containsKey(level)) {
            int max = Math.max(newScore, levelScores.get(level));
            levelScores.put(level, max);
            levelsJSON.put(level, max);
        }
        else {
            levelScores.put(level, newScore);
            levelsJSON.put(level, newScore);
        }
        saveHelper();
    }

    /**
     * Adds a new path at a specific level for replay later - probably the high score path
     * @param level - the level the path occured at
     * @param path - the list of points indicating the path the player took
     * @throws ReadSaveException - thrown if there is a problem saving or reading from the user file
     */
    public void updateLevelPath(int level, List<Double> path) {
        if (path == null || level < 1) return;
        JSONObject pathsJSON = (JSONObject) json.get("paths");
        if (levelPaths.containsKey(level)) {
            if (levelPaths.get(level).size() > path.size()) {
                levelPaths.put(level, path);
                pathsJSON.put(level, path);
            }
        } else {
            levelPaths.put(level, path);
            pathsJSON.put(level, path);
        }
        saveHelper();
    }

    public void setType(Integer newType) {
        if (newType == 0 || newType == 1) {
            type = newType;
        }
    }

    public Integer getType() {
        if (type == 0) return 0;
        if (type == 1) return 1;
        return 0;
    }

    /**
     * @return - the user's id
     */
    public String getId() {
        return id;
    }

    /**
     * @return - the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return - an immutable list of the (Month, Day, Year)
     */
    public List<Integer> getBirthday() {
        return ImmutableList.copyOf(birthday);
    }

    /**
     * @return - an int of the player's birth month
     */
    public int getBDayMonth() {
        return birthday.get(0);
    }

    /**
     * @return - an int of the player's birth day
     */
    public int getBDayDay() {
        return birthday.get(1);
    }

    /**
     * @return - an int of the player's birth year
     */
    public int getBDayYear() {
        return birthday.get(2);
    }

    /**
     * @return - a string of the filepath for the player's avatar
     */
    public String getAvatar() {
        return avatarImg;
    }

    /**
     * @return - the player's score
     */
    public int getScore() {
        return score;
    }

    /**
     * @param level - the level of the score
     * @return - the player's score on that level (0 if the player hasnt reached the level yet)
     */
    public int getLevel(int level) {
        if (levelScores.containsKey(level)) return levelScores.get(level);
        return 0;
    }

    /**
     * @return - an immutable map of all of the levels and what the player scored on them
     */
    public Map<Integer, Integer> getLevelScores() {
        return ImmutableMap.copyOf(levelScores);
    }

    /**
     * @return - an immutable map of the player's best paths on each level
     */
    public Map<Integer, List<Double>> getPaths() {
        Map<Integer, List<Double>> returnPaths = new HashMap<>();
        for (int key : levelPaths.keySet()) {
            returnPaths.put(key, ImmutableList.copyOf(levelPaths.get(key)));
        }
        return ImmutableMap.copyOf(returnPaths);
    }

    /**
     * @param level - the level that you want the player's path for
     * @return - the path for the level or an immutable list with 0.0, 0.0 as points if the level hasnt been attempted yet
     */
    public List<Double> getPath(int level) {
        if (levelPaths.containsKey(level)) return ImmutableList.copyOf(levelPaths.get(level));
        return ImmutableList.copyOf(Arrays.asList(0.0, 0.0));
    }

    /**
     * @return - the player's json object that will be saved - for testing only
     */
    public JSONObject getJSON() {
        return json;
    }

    /**
     * Resets the player's instance variables to what they were when the object was instantiated - for testing only
     * @throws ReadSaveException
     */
    public void reset() {
        json = original.getJson();
        id = original.getID();
        password = original.getPassword();
        avatarImg = original.getAvatar();
        birthday = original.getBirthday();
        levelPaths = original.getLevelPaths();
        levelScores = original.getLevelScores();
        score = original.getScore();
        Map<Integer, Integer> oldScores = original.getLevelScores();
        saveHelper();
    }
}
