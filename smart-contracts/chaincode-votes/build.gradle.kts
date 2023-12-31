import java.net.URI

plugins {
    application
}

application {
    mainClass.set("org.hyperledger.fabric.contract.ContractRouter")
}

repositories {
    maven { url = URI("https://jitpack.io") }
}

dependencies {
    api(project(":chaincode-commons"))
    implementation(libs.fabric.chaincode.shim)
    implementation(libs.genson)
    implementation(libs.json)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
}
