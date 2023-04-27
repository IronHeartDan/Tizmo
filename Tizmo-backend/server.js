import express from "express"
import bodyParser from "body-parser"
import Redis from "ioredis"

// Express
const app = express()
app.use(bodyParser.json())
const PORT = process.env.PORT || 3000

// Redis
const REDIS_URL = process.env.REDIS_URL || "localhost:6379"
const client = new Redis(REDIS_URL)
client.on('error', err => console.log('Redis Client Error', err))
client.on('connect', () => console.log("Redis Connected"))

// Firebase
import { verifyAuth, messaging } from "./firebase.js"


try {
    startServer()
} catch (error) {
    console.error(error);
}

async function startServer() {

    app.get("/health", (req, res) => {
        res.status(200).send("OK")
    })

    app.post("/notify", async (req, res) => {
        const { title, body, token } = req.body
        try {
            const sent = await messaging.send({
                token: token,
                android: {
                    notification: {
                        title: title,
                        body: body,
                    }
                }
            })
            console.log(sent);
            res.status(200).send("OK")
        } catch (error) {
            console.error(error);
            res.status(500).send("Internal Server Error")
        }
    })

    app.post("/location", verifyAuth, (req, res) => {
        const { user_id, longitude, latitude } = req.body;
        client.geoadd("locations", longitude, latitude, user_id, (err, _) => {
            if (err) {
                console.error(err)
                res.status(500).send("Internal Server Error")
            } else {
                client.zadd("users_track", Date.now(), user_id)
                res.status(200).send("OK")
            }
        })
    })

    app.get("/nearby", (req, res) => {
        const { user_id, radius } = req.query;

        client.georadiusbymember("locations", user_id, radius, "m", (err, people) => {
            if (err) {
                console.error(err)
                res.status(500).send("Internal Server Error")
            } else {
                res.status(200).send(people)
            }
        })
    })

    app.listen(PORT, () => {
        console.log(`Server running on ${PORT}`)
    })
}