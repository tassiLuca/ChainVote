import {Server} from "node:http";
import {Server as SocketIoServer} from "socket.io";

const SocketIoConfig = (httpServer: Server): SocketIoServer => {
    const io = new SocketIoServer(httpServer, {
        cors: {
            origin: "*",
        }
    });
    io.on("connection", (socket) => {
        /* On joinRoom event, the socket joins the room passed as parameter. */
        socket.on("joinRoom", (room: string) => {
            socket.join(room);
        });
    });
    return io;
}

export default SocketIoConfig;
