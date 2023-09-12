import {readFileSync} from "fs";
import jwt, {SignOptions} from 'jsonwebtoken';
import {BadRequestError} from "../..";

export type ConfigurationObject = {
    ATPrivateKeyPath?: string;
    ATPublicKeyPath?: string;
    RTPrivateKeyPath?: string;
    RTPublicKeyPath?: string;
};

export class JwtHandler {

    private static INSTANCE: JwtHandler | null = null;
    private internalConfiguration: ConfigurationObject;

    private signOptions: SignOptions = {
        issuer:   "ChainVote",
        audience: "https://www.chainvote.com",
        algorithm: "RS256"
    };

    private constructor(config: ConfigurationObject | null | undefined = undefined) {
        if(!config) {
            this.internalConfiguration = {}
        } else {
            this.internalConfiguration = config;
        }
    }
    public static config(config: ConfigurationObject): void {
        this.INSTANCE = new JwtHandler(config);
    }

    public static getInstance(config: ConfigurationObject | {}  = {}): JwtHandler {
        if(!this.INSTANCE) {
            this.INSTANCE = new JwtHandler(config);
        }
        return this.INSTANCE;
    }

    public setConfig(key: string, value: any): void {
        this.internalConfiguration[key] = value;
    }

    public getConfig(key: string): any {
        return this.internalConfiguration[key];
    }

    public verifyAccessToken<T>(token: string): T | undefined {
        const publicKey: string = this.getKey("PU", "AT");
        return this.verifyJwt(token, publicKey) as T
    }

    public verifyRefreshToken<T>(token: string): T | undefined {
        const publicKey: string = this.getKey("PU", "RT");
        const verifiedResponse = this.verifyJwt(token, publicKey) as T
        // @ts-ignore
        const subscriber = verifiedResponse.sub;
        if(!subscriber) {
            throw new Error("Unable to verify the identity of the subscriber");
        }
        return verifiedResponse;
    }

    public signAccessToken(user, expiration: string | undefined = undefined) {
        const privateKey: string = this.getKey("PR", "AT");
        if(!expiration) {
            expiration = "1m";
        }
        return this.signJwt({sub: user}, privateKey, {expiresIn: expiration});
    }

    public signRefreshToken(user, expiration: string | undefined = undefined): string {
        const privateKey = this.getKey("PR", "RT");
        if(!expiration) {
            expiration = "10m";
        }
        return this.signJwt({sub: user}, privateKey, {expiresIn: expiration});
    }

    private getKey(keyType: "PU" | "PR", tokenType: "AT" | "RT") : string {
        let keyPath: string | undefined;

        if(tokenType === "AT") {
            if(keyType === "PR") {
                keyPath = this.internalConfiguration.ATPrivateKeyPath || process.env.AT_PRIVATE_KEY_PATH;
            } else {
                keyPath = this.internalConfiguration.ATPublicKeyPath || process.env.AT_PUBLIC_KEY_PATH;
            }
        } else {
            if(keyType === "PR") {
                keyPath = this.internalConfiguration.RTPrivateKeyPath || process.env.RT_PRIVATE_KEY_PATH;
            } else {
                keyPath = this.internalConfiguration.RTPublicKeyPath || process.env.RT_PUBLIC_KEY_PATH;
            }
        }

        if(!keyPath) {
            throw new Error(`Key path of type ${keyPath} for token type ${tokenType} is not set`);
        }

        return readFileSync(keyPath, 'utf8');
    }

    private signJwt(payload: Object, pKey: string, options: SignOptions) {
        if(!("sub" in payload)) {
            throw new Error("sub property should be present in the payload, assign the User class that is making the reuqest");
        }
        try {
            return jwt.sign(payload, pKey, {...this.signOptions, ...options});
        } catch(error) {
            throw new Error("Error while creating a new payload: " + error.message);
        }
    }

    private verifyJwt<T>(token: string, pKey: string): T | null {
        return jwt.verify(token, pKey, (error, decoded) => {
            if(error) {
                throw new BadRequestError(error.message);
            }
            return decoded;
        }) as T;
    }
}

