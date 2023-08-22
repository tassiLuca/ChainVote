import mongoose, {Schema} from "mongoose"
import bcrypt from "bcrypt"

const SALT_WORK_FACTOR: number = 10

enum Roles {
  ADMIN="admin",
  USER="user"
}

interface IUser {
    email: string;
    password: string;
    firstName: string;
    secondName: string;
    role: Roles;
}

interface IUserDocument extends IUser, Document {
  comparePassword: (password: string) => Promise<boolean>;
}

let User = new Schema<IUserDocument>({
    email: {
        type: String,
        required: true, 
        index: {
            unique: true
        }
    },
    password: { 
        type: String, 
        required: true 
    },
    firstName: String,
    secondName: String,
    role: {
      type: String,
      enum: Roles,
      default: Roles.USER
    }
});


User.pre("save", function (next) {
    const user = this;

    if (user.isModified("password") || user.isNew) {    

      bcrypt.genSalt(SALT_WORK_FACTOR, function (error, salt) {
        if (error) {
          return next(error)
        } else {

          bcrypt.hash(user.password, salt, function(error, hash) {
            if (error) {
              return next(error)
            }
  
            user.password = hash
            next()
          })
        }
      })
    } else {
      return next()
    }
});

User.methods.comparePassword = function (password: string | Buffer, next: (err: Error | undefined, same: any) => any) {
  bcrypt.compare(password, this.password, function(error, isMatch) {
      if (error) {
          return next(error, undefined);
      }
      next(undefined, isMatch);
  });
};

const UserModel = mongoose.model<IUserDocument>('Users', User);

export {UserModel as User}
