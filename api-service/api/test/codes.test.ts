import {Jwt, JwtHandler, User} from "core-components";
import {StatusCodes} from "http-status-codes";
import ServerConfig from "../src/configs/server.config";
import request from "supertest";
import {createCodeForElection, createElection} from "./common/utils";
import {resolve} from "path";

let user,admin;
let userJwtToken, adminJwtToken;

let app;
const MAX_TIMEOUT = 20_000;

JwtHandler.config({
    ATPrivateKeyPath: resolve("./secrets/at_private.pem"),
    RTPrivateKeyPath: resolve("./secrets/rt_private.pem")
});

beforeAll(async () => {
    app = ServerConfig();
});

beforeEach(async () => {
    user = await new User({
        email: "fake.email@email.it",
        password: "Password1!",
        firstName: "Test",
        secondName: "User"
    }).save();

    admin = await new User({
        email: "admin.email1@email.it",
        password: "Password1!",
        firstName: "Test",
        secondName: "User",
        role: "admin"
    }).save();

    userJwtToken = await Jwt.createTokenPair(user, {accessToken: "10m", refreshToken: "20m"});
    adminJwtToken = await Jwt.createTokenPair(admin, {accessToken: "10m", refreshToken: "20m"});
});

afterEach(async () => {
    await User.deleteMany({});
    await Jwt.deleteMany({});
});



describe("POST /code/", () => {

    test("Can create a new code", async () => {
        const electionId = await createElection(app, adminJwtToken.accessToken);
        await createCodeForElection(app, electionId, userJwtToken.accessToken);
    }, MAX_TIMEOUT);

    test("Can check if a code is valid", async () => {
        const electionId = await createElection(app, adminJwtToken.accessToken);
        const code = await createCodeForElection(app, electionId, userJwtToken.accessToken);

        console.log(code, user.id, electionId);
        const response = await request(app).post("/code/check")
            .send({code: code, electionId: electionId })
            .set("Authorization", `Bearer ${userJwtToken.accessToken}`)
            .set("Accept", "application/json")
            .expect("Content-Type", "application/json; charset=utf-8")
            .expect(StatusCodes.OK);

        expect(response.body.success).toBe(true);
        expect(response.body.data).toBe(true);
    }, MAX_TIMEOUT);


    test("Can verify code owner", async () => {
        const electionId = await createElection(app, adminJwtToken.accessToken);
        const code = await createCodeForElection(app, electionId, userJwtToken.accessToken);
        const response = await request(app).post("/code/verify-owner")
            .send({code: code, userId: user.id, electionId: electionId})
            .set("Authorization", `Bearer ${userJwtToken.accessToken}`)
            .set("Accept", "application/json")
            .expect("Content-Type", "application/json; charset=utf-8")
            .expect(StatusCodes.OK);

        expect(response.body.data).toBe(true);
    }, MAX_TIMEOUT);
});