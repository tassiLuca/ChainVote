const express = require('express');
const router = express.Router();
const apiController = require("../controllers/apiController");

router.get('/elections', apiController.getAllElections);
router.get('/elections/:electionId', apiController.getElection);

router.get('/elections/:id', apiController.getElection);

module.exports = router;
