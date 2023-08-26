import { Request, Response, NextFunction } from "express";
import { User } from "core-components";
import { UnauthorizedError } from "core-components"

export async function login(req: Request, res: Response, next: NextFunction) {
    
    const email = req.body.email;
    const password = req.body.password;
    const responseError = new UnauthorizedError("Login error"); 

    try {
        const user = await User.findOne({email: email});
        if (user === null || user === undefined) {
            return next(responseError); 
        }
        
        user.comparePassword(password, function(error, isMatch) {
            if (error || !isMatch) {
                return next(responseError);
            }
            
            return res.status(200).send({
                message: "Login successfull",
                email: email
            });
        });
       
    } catch(error) {
        next(error);      
    }
}