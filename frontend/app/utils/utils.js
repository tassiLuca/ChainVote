const axios = require('axios');
const chaincodeErrorType = '1'
const badRequestErrorCode = '400'
const badRequestErrorMessage = 'Bad request'
const castVoteSuccessfulMessage = "Vote cast successfully."
const createElectionSuccessfulMessage = "Election created successfully."
const signUpSuccessfulMessage = "User successfully created."
const signInSuccessfulMessage = "User successfully logged in."


const axiosRequest = async (method, url, data = null, token = null) => {
    try {
        const config = {
            method: method,
            url: url,
            headers: {},
        };
        if (data) {
            config.data = data;
        }
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        const response = await axios(config);
        return response.data;
    } catch (error) {
        console.error(`Error making API call to ${url}:`, error.response.data);
        throw error;
    }
};

module.exports = {
    axiosRequest,
    chaincodeErrorType,
    badRequestErrorCode,
    badRequestErrorMessage,
    castVoteSuccessfulMessage,
    createElectionSuccessfulMessage,
    signUpSuccessfulMessage,
    signInSuccessfulMessage
}
