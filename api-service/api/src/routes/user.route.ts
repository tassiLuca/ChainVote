import { Router } from "express";
import { createUser, getProfile, editProfile, deleteProfile } from "../controllers/users";
import { authenticationHandler } from "core-components";
import { validationHandler } from "core-components";
import {body, param} from "express-validator";
import {ApiLimiterEntry,apiLimiter} from "core-components";
import RedisLimiterStorage from "../configs/redis.config";

const userRouter = Router();


const API_LIMITER_RULES: ApiLimiterEntry = {
    "/": {
        "GET": {
            time: 20,
            limit: 100
        },
        "PUT": {
            time: 20,
            limit: 100
        },
        "DELETE": {
            time: 20,
            limit: 100
        },
        "POST": {
            time: 10,
            limit: 100
        }
    },
}

const limitStorage = new RedisLimiterStorage();

userRouter.use(apiLimiter(API_LIMITER_RULES, limitStorage));

/**
 * @openapi
 *
 * paths:
 *   /users/{email}:
 *      get:
 *          summary: Return the profile of the user with the given email
 *          parameters:
 *              - name: email
 *                in: path
 *                description: The email of the user profile.
 *                required: false
 *                schema:
 *                  type: string
 *          responses:
 *              '200':
 *                  description: Ok
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/CommonResponse'
 *              '400':
 *                  description: Bad Request
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/BadRequestError'
 *
 *              '429':
 *                  description: Too many requests
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/TooManyRequestError'
 *              '500':
 *                  description: Generic server error
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/InternalServerError'
 *
 */
userRouter.get(
    "/:email",
    authenticationHandler,
    validationHandler([
        param("email").exists().isEmail()
    ]),
    getProfile);

/**
 * @openapi
 *
 * paths:
 *   /users/{userEmail}:
 *      put:
 *          summary: Update a specific user
 *          parameters:
 *              - name: userEmail
 *                in: path
 *                description: The email of the user to update.
 *                required: false
 *                schema:
 *                  type: string
 *              - name: data
 *                in: body
 *                description: The data to update the user with.
 *                required: true
 *                schema:
 *                  type: object
 *                  properties:
 *                      firstName:
 *                          type: string
 *                          description: The first name of the user.
 *                          example: "Jean"
 *                          required: false
 *                      secondName:
 *                          type: string
 *                          description: The second name of the user.
 *                          example: "Paù"
 *                          required: false
 *          responses:
 *              '200':
 *                  description: Ok
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/CommonResponse'
 *              '400':
 *                  description: Bad Request
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/BadRequestError'
 *
 *              '429':
 *                  description: Too many requests
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/TooManyRequestError'
 *              '500':
 *                  description: Generic server error
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/InternalServerError'
 *
 */
userRouter.put(
    "/:email",
    authenticationHandler,
    validationHandler([
        // Param validation
        param("email").exists().isEmail(),

        // Body validation
        body("data").isObject().notEmpty(),
        body("data.firstName").optional().isAlpha(),
        body("data.secondName").optional().isAlpha(),

        // Check that the user is not trying to change the email or the password
        body("data.password").not().exists(),
        body("data.email").not().exists(),
    ]),
    editProfile);


/**
 * @openapi
 *
 * paths:
 *   /users/{userEmail}:
 *      delete:
 *          summary: Update a specific user
 *          parameters:
 *              - name: email
 *                in: body
 *                description: The email of the user to delete.
 *                required: true
 *                schema:
 *                  type: string
 *          responses:
 *              '200':
 *                  description: Ok
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/CommonResponse'
 *              '400':
 *                  description: Bad Request
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/BadRequestError'
 *
 *              '429':
 *                  description: Too many requests
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/TooManyRequestError'
 *              '500':
 *                  description: Generic server error
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/InternalServerError'
 *
 */
userRouter.delete(
    "/",
    authenticationHandler,
    validationHandler([
        body("email").exists().isEmail()
    ]),
    deleteProfile);

/**
 * @openapi
 *
 * paths:
 *   /users:
 *      post:
 *          summary: Create a new user
 *          requestBody:
 *              required: true
 *              content:
 *                  application/json:
 *                      schema:
 *                          type: object
 *                          properties:
 *                              email:
 *                                  type: string
 *                                  description: The email of the user
 *                              password:
 *                                  type: string
 *                                  description: The password of the user, this will be encrypted.
 *                              firstName:
 *                                  type: string
 *                                  description: The first name of the user.
 *                              secondName:
 *                                  type: string
 *                                  description: The second name of the user.
 *          responses:
 *              '200':
 *                  description: Ok
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/CommonResponse'
 *              '400':
 *                  description: Bad Request
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/BadRequestError'
 *
 *              '429':
 *                  description: Too many requests
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/TooManyRequestError'
 *              '500':
 *                  description: Generic server error
 *                  content:
 *                      application/json:
 *                          schema:
 *                              allOf:
 *                                  - $ref: '#/components/schemas/InternalServerError'
 *
 */
userRouter.post(
    "/",
    validationHandler([
        body("email").exists().isEmail(),
        body("password").exists().isStrongPassword(),
        body("firstName").exists().isAlpha(),
        body("secondName").exists().isAlpha(),
    ]),
    createUser);

export default userRouter;