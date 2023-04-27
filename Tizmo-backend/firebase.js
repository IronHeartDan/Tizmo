import Firebase from "firebase-admin"

import serviceAccount from "./serviceAccountKey.json" assert { type: 'json' };

const firebase = Firebase.initializeApp({
    credential: Firebase.credential.cert(serviceAccount)
})

const auth = firebase.auth()
const messaging = firebase.messaging()

const verifyAuth = async (req, res, next) => {
    let token = req.header("Authorization");
    if (!token) return res.status(401).send("Access Denied");

    try {
        await auth.verifyIdToken(token)
        next();
    } catch (error) {
        res.status(400).send("Invalid Token");
    }
}

export default firebase
export { auth, messaging, verifyAuth };