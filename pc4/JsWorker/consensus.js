const express = require('express');
const axios = require('axios');

let isLeader = false;
const peers = ['http://localhost:8080', 'http://localhost:8081'];

const startConsensus = () => {
    setInterval(async () => {
        if (isLeader) return;

        const votes = await Promise.all(peers.map(async (peer) => {
            try {
                const response = await axios.post(`${peer}/requestVote`, { term: Date.now() });
                return response.data.voteGranted;
            } catch (error) {
                return false;
            }
        }));

        if (votes.filter(vote => vote).length > peers.length / 2) {
            isLeader = true;
            console.log('I am the leader');
        }
    }, 5000);
};

const requestVote = (req, res) => {
    res.json({ voteGranted: true });
};

module.exports = { startConsensus, requestVote };
