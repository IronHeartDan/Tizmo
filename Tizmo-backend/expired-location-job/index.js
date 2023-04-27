import Redis from "ioredis";
import Bull from "bull";

// Redis
const REDIS_URL = process.env.REDIS_URL || "localhost:6379"
const client = new Redis(REDIS_URL)
client.on('error', err => console.log('Redis Client Error', err));
client.on('connect', () => console.log("Redis Connected"));

// Bull
const queue = new Bull('queue', REDIS_URL);

// Actual Job Implementation (to be executed by bull)
queue.process("remove-expired-locations", async (job, done) => {
    const now = Date.now();
    const tenSecondsAgo = now - 10000; // 10 seconds in milliseconds

    try {
        const expired = await new Promise((resolve, reject) => {
            client.zrangebyscore("users_track", 0, tenSecondsAgo, (err, expired) => {
                if (err) {
                    reject(err);
                } else {
                    resolve(expired);
                }
            });
        });
        console.log("To Expire", expired);
        if (expired.length > 0) {
            console.log("Expired", expired);
            const multi = client.multi();
            multi.zrem("users_track", ...expired);
            multi.zrem("locations", ...expired);
            await multi.exec();
        }
        done();
    } catch (err) {
        console.error(err);
        done(err);
    }
});

// Producer
queue.add('remove-expired-locations', null, {
    repeat: { cron: '*/10 * * * * *' } // execute every 10 seconds
})