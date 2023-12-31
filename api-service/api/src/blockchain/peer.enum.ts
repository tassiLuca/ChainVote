export enum Org1Peer {
    PEER1 = "peer1-org1:7051",
}

export enum Org2Peer {
    PEER1 = "peer1-org2:9051",
    PEER2 = "peer2-org2:10051"
}

export enum Org3Peer {
    PEER1 = "peer1-org3:20051",
    PEER2 = "peer2-org3:30051"
}

/**
 * Return the peer base name
 * @param peer The peer name
 */
export function getPeerBasename(peer: Org1Peer | Org2Peer | Org3Peer): string {
    return peer.split('-')[0];
}

/**
 * Return the peer host alias
 * @param peer The peer name
 */
export function getPeerHostAlias(peer: Org1Peer | Org2Peer | Org3Peer): string {
    return process.env.PRODUCTION ? peer.split(':')[0] : "localhost";
}

/**
 * Return the organization name of the peer
 * @param peer The peer name
 */
export function getPeerOrganization(peer: Org1Peer | Org2Peer | Org3Peer): string {
    const match: RegExpMatchArray = peer.match(/-([^-:]+):/) as RegExpMatchArray;
    return match[1];
}


/**
 * Return the port of the peer
 * @param peer the peer name
 */
function getPeerPort(peer: Org1Peer | Org2Peer | Org3Peer): string {
    return peer.split(':')[1];
}

/**
 * Return the host address of the peer
 * @param peer the peer name
 */
export function getPeerHost(peer: Org1Peer | Org2Peer | Org3Peer): string {
    return `${getPeerHostAlias(peer)}:${getPeerPort(peer)}`;
}
