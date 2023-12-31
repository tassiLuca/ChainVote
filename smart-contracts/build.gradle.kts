import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.Properties
import kotlin.io.path.Path

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
}

subprojects {

    apply(plugin = "java-library")

    group = "it.unibo.ds.chainvote"

    java {
        // Hyperledger Fabric chaincode library works only with Java 11!
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        with(rootProject.libs) {
            implementation(log4j.api)
            implementation(log4j.core)
            implementation(log4j.slf4j.impl)
            compileOnly(spotbugs.annotations)
            testCompileOnly(spotbugs.annotations)
            testImplementation(junit.api)
            testImplementation(junit.engine)
        }
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.javadoc {
        val javadocDir = file(Path(rootProject.projectDir.absolutePath, "build", "javadoc", project.name))
        javadocDir.mkdirs()
        setDestinationDir(javadocDir)
    }
}

fun File.envValueOf(property: String): String = Properties()
    .apply { this@envValueOf.inputStream().use { load(it) } }
    .getProperty(property)

fun File.resolveAll(vararg others: String): File = others.fold(this) { f, s -> f.resolve(s) }

val blockchainGroup = "blockchain"

typealias Channel = String
val channel1 = "ch1"
val channel2 = "ch2"

typealias Organization = String
val organization1 = "org1"
val organization2 = "org2"
val organization3 = "org3"

data class Chaincode(val name: String, val organizations: Set<Organization>)
val electionsChaincode = Chaincode("chaincode-elections", setOf(organization1))
val votesChaincode = Chaincode("chaincode-votes", setOf(organization2, organization3))

data class Peer(val organization: Organization, val name: String, val address: String)
val peersOrg1 = setOf(
    Peer(organization1, "peer1", "localhost:7051"),
)
val peersOrg2 = setOf(
    Peer(organization2, "peer1", "localhost:9051"),
    Peer(organization2, "peer2", "localhost:10051"),
)
val peersOrg3 = setOf(
    Peer(organization3, "peer1", "localhost:20051"),
    Peer(organization3, "peer2", "localhost:30051")
)
val allPeers = peersOrg1.plus(peersOrg2).plus(peersOrg3)

val blockchainDirectory: File = Path(projectDir.parent, "blockchain").toFile()
val configurationFile: File = File(projectDir.parent, "config.env")
val artifactsDirectory: File = File(projectDir.parent, configurationFile.envValueOf("ARTIFACTS_DIR"))
val networkScript: File = blockchainDirectory.resolve("network.sh")
val peerScript: File = blockchainDirectory.resolveAll("bin", "peer")

fun commonEnvironments() = mapOf(
    "CORE_PEER_TLS_ENABLED" to "true",
    "FABRIC_CFG_PATH" to blockchainDirectory.resolveAll("channels_config", "generated").absolutePath
)

fun environmentsFor(peer: Peer) = mapOf(
    "CORE_PEER_LOCALMSPID" to "${peer.organization}MSP",
    "CORE_PEER_MSPCONFIGPATH" to artifactsDirectory.resolveAll(peer.organization, "admin", "msp").absolutePath,
    "CORE_PEER_TLS_ROOTCERT_FILE" to artifactsDirectory.resolveAll(peer.organization, peer.name, "assets", "tls-ca", "tls-ca-cert.pem").absolutePath,
    "CORE_PEER_ADDRESS" to peer.address,
).plus(commonEnvironments())

fun executeCommand(
    command: String,
    directory: File = blockchainDirectory,
    environments: Map<String, String>? = null,
    stdout: OutputStream? = null,
) = exec {
    workingDir = directory
    stdout?.let { standardOutput = it }
    environments?.let { environment(it) }
    commandLine(command.split(" "))
}

tasks.register("downNetwork") {
    group = blockchainGroup
    description = "Bring down the blockchain network without cleaning artifacts"
    doLast { executeCommand("$networkScript down") }
}

tasks.register("downNetworkAndClean") {
    group = blockchainGroup
    description = "Bring down the blockchain network and clean the artifacts"
    doLast { executeCommand("$networkScript clean") }
}

tasks.register("upNetwork") {
    group = blockchainGroup
    description = "Bring up the blockchain network"
    dependsOn("downNetwork")
    doLast { executeCommand("$networkScript up") }
}

fun Chaincode.`package`() = executeCommand(
    "$peerScript lifecycle chaincode package $name.tar.gz " +
        "--path ${projectDir.resolveAll(name, "build", "install", name)} " +
        "--lang java " +
        "--label ${name}_1.0",
    environments = commonEnvironments(),
)

tasks.register("packageChaincodes") {
    group = blockchainGroup
    description = "Build and generate chaincodes packages"
    dependsOn(":${electionsChaincode.name}:installDist", ":${votesChaincode.name}:installDist", "upNetwork")
    doLast {
        electionsChaincode.`package`()
        votesChaincode.`package`()
    }
}

tasks.register<Delete>("cleanAllPackages") {
    group = blockchainGroup
    description = "Remove all generated packages"
    blockchainDirectory.listFiles { _, fileName -> fileName.endsWith(".tar.gz") }?.forEach { delete(it) }
}

infix fun Chaincode.installOn(peers: Set<Peer>) = peers.forEach {
    println(">> Installing $name on $it")
    val outputStream = ByteArrayOutputStream()
    executeCommand(
        "$peerScript lifecycle chaincode install $name.tar.gz",
        environments = environmentsFor(it),
        stdout = outputStream,
    )
}

fun Chaincode.packageId(): String? {
    val outputStream = ByteArrayOutputStream()
    val representativeOrganization = organizations.first()
    val representativePeer = allPeers
        .find { it.organization == representativeOrganization }
        ?: error("No peer found for $representativeOrganization")
    executeCommand(
        "$peerScript lifecycle chaincode queryinstalled",
        environments = environmentsFor(representativePeer),
        stdout = outputStream,
    )
    return Regex("$name[^,]+").find(outputStream.toString())?.value
}

fun Chaincode.approveFor(peers: Set<Peer>, channels: Set<Channel>, collectionsConfig: File? = null) {
    val packageId = packageId() ?: error("No package found for $name")
    peers.map { it.organization }.distinct().forEach { org ->
        println(">> Approval for $org")
        val approvalPeer = peers.find { it.organization == org }!!
        channels.forEach { channel ->
            executeCommand(
                "$peerScript lifecycle chaincode approveformyorg " +
                    "--orderer localhost:7050 " +
                    "--ordererTLSHostnameOverride orderer1-org0 " +
                    "--name $name " +
                    "--channelID $channel " +
                    "--version 1.0 " +
                    "--package-id $packageId " +
                    "--sequence 1 " +
                    "--cafile ${artifactsDirectory.resolveAll(org, approvalPeer.name, "tls-msp", "tlscacerts", "tls-0-0-0-0-7052.pem")} " +
                    (collectionsConfig?.let { "--collections-config ${it.absolutePath} " } ?: "") +
                    "--tls",
                environments = environmentsFor(approvalPeer),
            )
        }
    }
}

fun Chaincode.commit(peers: Set<Peer>, collectionsConfig: File? = null) {
    println(">> Committing")
    val representativeOrganization = organizations.first()
    val approvalPeer = peers.find { it.organization == representativeOrganization } ?: error("No peers found")
    val channel = "ch${representativeOrganization.last()}"
    val peerAddresses = peers.filter { it.organization == representativeOrganization }.joinToString(separator = " ") {
        "--peerAddresses ${it.address} " +
            "--tlsRootCertFiles ${artifactsDirectory.resolveAll(it.organization, it.name, "tls-msp", "tlscacerts", "tls-0-0-0-0-7052.pem")}"
    }
    executeCommand(
        "$peerScript lifecycle chaincode commit " +
            "--orderer localhost:7050 " +
            "--ordererTLSHostnameOverride orderer1-org0 " +
            "--channelID $channel " +
            "--name $name " +
            "--version 1.0 " +
            "--sequence 1 " +
            "--cafile ${artifactsDirectory.resolveAll(representativeOrganization, approvalPeer.name, "tls-msp", "tlscacerts", "tls-0-0-0-0-7052.pem")} " +
            "$peerAddresses " +
            (collectionsConfig?.let { "--collections-config ${it.absolutePath} " } ?: "") +
            "--tls",
        environments = environmentsFor(approvalPeer),
    )
}

fun Chaincode.deploy(peers: Set<Peer>, channels: Set<Channel>, collectionsConfig: File? = null) {
    installOn(peers)
    approveFor(peers, channels, collectionsConfig)
    commit(peers, collectionsConfig)
}

tasks.register("upAndDeploy") {
    group = blockchainGroup
    description = "Up the network and deploy chaincodes in one shot"
    dependsOn("upNetwork", "packageChaincodes")
    finalizedBy("cleanAllPackages")
    doLast {
        electionsChaincode.deploy(allPeers, setOf(channel1))
        votesChaincode.apply {
            deploy(
                peersOrg2.plus(peersOrg3),
                setOf(channel1, channel2),
                projectDir.resolveAll(name, "src", "main", "resources", "collections-config.json")
            )
        }
    }
}

tasks.register("installBinaries") {
    description = "Install Hyperledger Fabric binaries"
    doLast { executeCommand(blockchainDirectory.resolve("install-binaries.sh").toString()) }
}

tasks.filter { it.group == blockchainGroup }.forEach {
    it.dependsOn("installBinaries")
}
