package db

import (
	"context"
	"fmt"
	"github.com/go-redis/redis/v7"
	"github.com/vmwarecloudadvocacy/user/pkg/logger"
	mgo "go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"os"
)

var (
	mongo *mgo.Client

	DB *mgo.Database

	Collection *mgo.Collection

	Context *context.Context

	RedisClient *redis.Client
)

// GetEnv accepts the ENV as key and a default string
// If the lookup returns false then it uses the default string else it leverages the value set in ENV variable
func GetEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}

	logger.Logger.Info("Setting default values for ENV variable " + key)
	return fallback
}

// ConnectRedisDB returns a redis client
func ConnectRedisDB() *redis.Client {

	redisHost := GetEnv("REDIS_HOST", "0.0.0.0")
	redisPort := GetEnv("REDIS_PORT", "6379")
	redisPassword := GetEnv("REDIS_PASSWORD", "secret")
	redisConnStr := GetEnv("REDIS_CONNECTIONSTRING", "")

	redisAddr := fmt.Sprintf("%s:%s", redisHost, redisPort)

	redisOpt := &redis.Options{
		Addr:     redisAddr,
		Password: redisPassword,
		DB:       0,
	}

	if len(redisConnStr) > 0 {
		redisOpt, _ = redis.ParseURL(redisConnStr)
	}

	RedisClient = redis.NewClient(redisOpt)

	pong, err := RedisClient.Ping().Result()
	logger.Logger.Infof("Reply from Redis %s", pong)
	if err != nil {
		logger.Logger.Fatalf("Failed connecting to redis db %s", err.Error())
		os.Exit(1)
	}
	logger.Logger.Infof("Successfully connected to redis database")
	return RedisClient
}

// ConnectDB accepts name of database and collection as a string
func ConnectDB(dbName string, collectionName string, ctx context.Context) *mgo.Client {

	dbUsername := os.Getenv("USERS_DB_USERNAME")
	dbSecret := os.Getenv("USERS_DB_PASSWORD")
	mongoConnStr := GetEnv("MONGODB_CONNECTIONSTRING", "")

	// Get ENV variable or set to default value
	dbIP := GetEnv("USERS_DB_HOST", "0.0.0.0")
	dbPort := GetEnv("USERS_DB_PORT", "27017")

	mongoDBUrl := fmt.Sprintf("mongodb://%s:%s@%s:%s/?authSource=admin", dbUsername, dbSecret, dbIP, dbPort)

	if len(mongoConnStr) > 0 {
		mongoDBUrl = mongoConnStr
	}

	Client, error := mgo.Connect(ctx, options.Client().ApplyURI(mongoDBUrl))

	if error != nil {
		fmt.Printf(error.Error())
		logger.Logger.Fatalf(error.Error())
		os.Exit(1)
	}

	DB = Client.Database(dbName)
	Context = &ctx

	//error = DB.
	//if error != nil {
	//	logger.Logger.Errorf("Unable to connect to database %s", dbName)
	//}

	Collection = DB.Collection(collectionName)

	logger.Logger.Info("Connected to database and the collection")

	return Client
}

// CloseDB accept Session as input to close Connection to the database
func CloseDB(client *mgo.Client, ctx context.Context) {

	client.Disconnect(ctx)

	defer logger.Logger.Info("Closed connection to db")
}
