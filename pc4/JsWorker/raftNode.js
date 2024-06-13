class RaftNode {
    constructor(nodeId, workerAddresses) {
        this.nodeId = nodeId;
        this.workerAddresses = workerAddresses;
        this.state = 'follower'; // Inicializamos como seguidor
        this.leaderId = null;
    }

    run() {
        // Inicia la lógica de consenso Raft simplificada
        setInterval(() => {
            // Simula el proceso de elección de líder
            if (Math.random() > 0.5) {
                this.state = 'leader';
                this.leaderId = this.nodeId;
                console.log(`${this.nodeId} se convirtió en el líder`);
            } else {
                this.state = 'follower';
            }
        }, 5000); // Intervalo de simulación de 5 segundos
    }

    getState() {
        return this.state;
    }
}

module.exports = RaftNode;
