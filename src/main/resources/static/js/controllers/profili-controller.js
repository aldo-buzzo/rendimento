/**
 * Controller per la gestione dei profili utente.
 * Implementa solo le funzionalità essenziali per visualizzare i profili in forma tabellare.
 */
document.addEventListener('DOMContentLoaded', function() {
    // Verifica autenticazione
    Auth.isAuthenticated().then(authenticated => {
        if (!authenticated) {
            window.location.href = 'login.html?redirect=profili-utente.html';
            return;
        }
        
        // Inizializza il controller solo se l'utente è autenticato
        initController();
    });
    
    function initController() {
        // Elementi DOM
        const profiliList = document.getElementById('profili-list');
        const profiliLoading = document.getElementById('profili-loading');
        const profiliError = document.getElementById('profili-error');
        const profiliEmpty = document.getElementById('profili-empty');
        const nuovoProfiloBtn = document.getElementById('nuovo-profilo-btn');
        const profiloModal = new bootstrap.Modal(document.getElementById('profilo-modal'));
        const profiloForm = document.getElementById('profilo-form');
        const saveProfiloBtn = document.getElementById('save-profilo-btn');
        
        // Elementi del form
        const profiloId = document.getElementById('profilo-id');
        const nomeProfilo = document.getElementById('nome-profilo');
        const periodicitaBollo = document.getElementById('periodicita-bollo');
        const percentualeBollo = document.getElementById('percentuale-bollo');
        const commissioneBtp = document.getElementById('commissione-btp');
        const commissioneBot120gg = document.getElementById('commissione-bot-120gg');
        const commissioneBot240gg = document.getElementById('commissione-bot-240gg');
        const commissioneBotOltre = document.getElementById('commissione-bot-oltre');
        const commissioneCct = document.getElementById('commissione-cct');
        const commissioneCtz = document.getElementById('commissione-ctz');
        
        // Carica i profili
        caricaProfili();
        
        // Event listeners
        nuovoProfiloBtn.addEventListener('click', mostraNuovoProfilo);
        saveProfiloBtn.addEventListener('click', salvaProfilo);
        
        /**
         * Carica i profili dell'utente autenticato
         */
        function caricaProfili() {
            mostraLoading(true);
            
            ApiService.get('/api/profili-calcolo')
                .then(profili => {
                    mostraLoading(false);
                    
                    if (profili && profili.length > 0) {
                        renderizzaProfili(profili);
                    } else {
                        profiliEmpty.classList.remove('d-none');
                    }
                })
                .catch(error => {
                    console.error('Errore nel caricamento dei profili:', error);
                    mostraLoading(false);
                    profiliError.classList.remove('d-none');
                });
        }
        
        /**
         * Renderizza i profili nella tabella
         * @param {Array} profili - Array di profili da visualizzare
         */
        function renderizzaProfili(profili) {
            profiliList.innerHTML = '';
            
            profili.forEach(profilo => {
                const row = document.createElement('tr');
                
                // Formatta i valori percentuali (moltiplica per 100 per convertire da decimale a percentuale)
                const formatPercentuale = (val) => val ? ((parseFloat(val) * 100).toFixed(2) + '%') : '-';
                
                // Formatta i valori booleani
                const formatBoolean = (val) => val === true ? 'Sì' : 'No';
                
                // Visualizza se il profilo è predefinito
                const isPredefinito = profilo.isDefault === true;
                const defaultIcon = isPredefinito ? 
                    '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-star-fill text-warning" viewBox="0 0 16 16"><path d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z"/></svg>' : 
                    '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-star" viewBox="0 0 16 16"><path d="M2.866 14.85c-.078.444.36.791.746.593l4.39-2.256 4.389 2.256c.386.198.824-.149.746-.592l-.83-4.73 3.522-3.356c.33-.314.16-.888-.282-.95l-4.898-.696L8.465.792a.513.513 0 0 0-.927 0L5.354 5.12l-4.898.696c-.441.062-.612.636-.283.95l3.523 3.356-.83 4.73zm4.905-2.767-3.686 1.894.694-3.957a.565.565 0 0 0-.163-.505L1.71 6.745l4.052-.576a.525.525 0 0 0 .393-.288L8 2.223l1.847 3.658a.525.525 0 0 0 .393.288l4.052.575-2.906 2.77a.565.565 0 0 0-.163.506l.694 3.957-3.686-1.894a.503.503 0 0 0-.461 0z"/></svg>';
                
                row.innerHTML = `
                    <td>${escapeHtml(profilo.nome || '')}</td>
                    <td class="text-center">${defaultIcon}</td>
                    <td>${formatPeriodicitaBollo(profilo.periodicitaBollo)}</td>
                    <td>${formatPercentuale(profilo.percentualeBollo)}</td>
                    <td>${formatPercentuale(profilo.commissioneBtp)}</td>
                    <td>${formatPercentuale(profilo.commissioneBot120gg)}</td>
                    <td>${formatPercentuale(profilo.commissioneBot240gg)}</td>
                    <td>${formatPercentuale(profilo.commissioneBotOltre)}</td>
                    <td>${formatPercentuale(profilo.commissioneCct)}</td>
                    <td>${formatPercentuale(profilo.commissioneCtz)}</td>
                    <td>${formatBoolean(profilo.plusvalenzaEsente)}</td>
                    <td>
                        <div class="btn-group btn-group-sm" role="group">
                            <button type="button" class="btn btn-outline-warning set-default-btn" data-id="${profilo.idProfilo}" title="Imposta come predefinito" ${isPredefinito ? 'disabled' : ''}>
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-star" viewBox="0 0 16 16">
                                    <path d="M2.866 14.85c-.078.444.36.791.746.593l4.39-2.256 4.389 2.256c.386.198.824-.149.746-.592l-.83-4.73 3.522-3.356c.33-.314.16-.888-.282-.95l-4.898-.696L8.465.792a.513.513 0 0 0-.927 0L5.354 5.12l-4.898.696c-.441.062-.612.636-.283.95l3.523 3.356-.83 4.73zm4.905-2.767-3.686 1.894.694-3.957a.565.565 0 0 0-.163-.505L1.71 6.745l4.052-.576a.525.525 0 0 0 .393-.288L8 2.223l1.847 3.658a.525.525 0 0 0 .393.288l4.052.575-2.906 2.77a.565.565 0 0 0-.163.506l.694 3.957-3.686-1.894a.503.503 0 0 0-.461 0z"/>
                                </svg>
                            </button>
                            <button type="button" class="btn btn-outline-primary edit-btn" data-id="${profilo.idProfilo}" title="Modifica">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil" viewBox="0 0 16 16">
                                    <path d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z"/>
                                </svg>
                            </button>
                            <button type="button" class="btn btn-outline-danger delete-btn" data-id="${profilo.idProfilo}" title="Elimina">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
                                    <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                                    <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"/>
                                </svg>
                            </button>
                        </div>
                    </td>
                `;
                
                profiliList.appendChild(row);
            });
            
            // Aggiungi event listeners ai pulsanti
            document.querySelectorAll('.edit-btn').forEach(btn => {
                btn.addEventListener('click', () => modificaProfilo(btn.dataset.id));
            });
            
            document.querySelectorAll('.delete-btn').forEach(btn => {
                btn.addEventListener('click', () => eliminaProfilo(btn.dataset.id));
            });
            
            document.querySelectorAll('.set-default-btn').forEach(btn => {
                btn.addEventListener('click', () => impostaProfiloPredefinito(btn.dataset.id));
            });
        }
        
        /**
         * Mostra il modal per un nuovo profilo
         */
        function mostraNuovoProfilo() {
            // Reset form
            profiloForm.reset();
            profiloId.value = '';
            document.getElementById('profilo-modal-label').textContent = 'Nuovo Profilo';
            
            // Imposta valori predefiniti solo per il bollo
            periodicitaBollo.value = 'ANNUALE';
            percentualeBollo.value = '0.2'; // 0.2% (formato percentuale per l'interfaccia utente)
            
            // Non impostiamo valori predefiniti per le commissioni
            commissioneBtp.value = '';
            commissioneBot120gg.value = '';
            commissioneBot240gg.value = '';
            commissioneBotOltre.value = '';
            commissioneCct.value = '';
            commissioneCtz.value = '';
            document.getElementById('plusvalenza-esente').checked = false;
            
            profiloModal.show();
        }
        
        /**
         * Carica i dati di un profilo per la modifica
         * @param {string} id - ID del profilo da modificare
         */
        function modificaProfilo(id) {
            ApiService.get(`/api/profili-calcolo/${id}`)
                .then(profilo => {
                    profiloId.value = profilo.idProfilo;
                    nomeProfilo.value = profilo.nome || '';
                    periodicitaBollo.value = profilo.periodicitaBollo || 'ANNUALE';
                    
                    // Converti i valori decimali in percentuali per l'interfaccia utente
                    percentualeBollo.value = profilo.percentualeBollo ? (parseFloat(profilo.percentualeBollo) * 100).toFixed(2) : '0.2';
                    commissioneBtp.value = profilo.commissioneBtp ? (parseFloat(profilo.commissioneBtp) * 100).toFixed(2) : '';
                    commissioneBot120gg.value = profilo.commissioneBot120gg ? (parseFloat(profilo.commissioneBot120gg) * 100).toFixed(2) : '';
                    commissioneBot240gg.value = profilo.commissioneBot240gg ? (parseFloat(profilo.commissioneBot240gg) * 100).toFixed(2) : '';
                    commissioneBotOltre.value = profilo.commissioneBotOltre ? (parseFloat(profilo.commissioneBotOltre) * 100).toFixed(2) : '';
                    commissioneCct.value = profilo.commissioneCct ? (parseFloat(profilo.commissioneCct) * 100).toFixed(2) : '';
                    commissioneCtz.value = profilo.commissioneCtz ? (parseFloat(profilo.commissioneCtz) * 100).toFixed(2) : '';
                    document.getElementById('plusvalenza-esente').checked = profilo.plusvalenzaEsente === true;
                    
                    document.getElementById('profilo-modal-label').textContent = 'Modifica Profilo';
                    profiloModal.show();
                })
                .catch(error => {
                    console.error('Errore nel caricamento del profilo:', error);
                    alert('Si è verificato un errore nel caricamento del profilo.');
                });
        }
        
        /**
         * Salva un profilo (nuovo o esistente)
         */
        function salvaProfilo() {
            if (!profiloForm.checkValidity()) {
                profiloForm.reportValidity();
                return;
            }
            
            // Ottieni il valore della commissione BTP per usarlo come default per le altre commissioni
            const commissioneBtpValue = parseFloat(commissioneBtp.value);
            
            // I valori nell'interfaccia utente sono in formato percentuale (es. 0.2 per 0.2%), 
            // quindi dobbiamo dividerli per 100 per ottenere il formato decimale (es. 0.002)
            const profilo = {
                idProfilo: profiloId.value || null,
                nome: nomeProfilo.value,
                periodicitaBollo: periodicitaBollo.value,
                percentualeBollo: parseFloat(percentualeBollo.value) / 100,
                commissioneBtp: commissioneBtpValue / 100,
                commissioneBot120gg: commissioneBot120gg.value ? parseFloat(commissioneBot120gg.value) / 100 : commissioneBtpValue / 100,
                commissioneBot240gg: commissioneBot240gg.value ? parseFloat(commissioneBot240gg.value) / 100 : commissioneBtpValue / 100,
                commissioneBotOltre: commissioneBotOltre.value ? parseFloat(commissioneBotOltre.value) / 100 : commissioneBtpValue / 100,
                commissioneCct: commissioneCct.value ? parseFloat(commissioneCct.value) / 100 : commissioneBtpValue / 100,
                commissioneCtz: commissioneCtz.value ? parseFloat(commissioneCtz.value) / 100 : commissioneBtpValue / 100,
                plusvalenzaEsente: document.getElementById('plusvalenza-esente').checked
            };
            
            const isNew = !profiloId.value;
            const method = isNew ? 'post' : 'put';
            const url = isNew ? '/api/profili-calcolo' : `/api/profili-calcolo/${profiloId.value}`;
            
            ApiService[method](url, profilo)
                .then(() => {
                    profiloModal.hide();
                    caricaProfili();
                })
                .catch(error => {
                    console.error('Errore nel salvataggio del profilo:', error);
                    alert('Si è verificato un errore nel salvataggio del profilo.');
                });
        }
        
        /**
         * Elimina un profilo
         * @param {string} id - ID del profilo da eliminare
         */
        function eliminaProfilo(id) {
            if (!confirm('Sei sicuro di voler eliminare questo profilo?')) {
                return;
            }
            
            ApiService.delete(`/api/profili-calcolo/${id}`)
                .then(() => {
                    caricaProfili();
                })
                .catch(error => {
                    console.error('Errore nell\'eliminazione del profilo:', error);
                    alert('Si è verificato un errore nell\'eliminazione del profilo.');
                });
        }
        
        /**
         * Imposta un profilo come predefinito
         * @param {string} id - ID del profilo da impostare come predefinito
         */
        function impostaProfiloPredefinito(id) {
            // Mostra un indicatore di caricamento
            mostraLoading(true);
            
            // Chiama l'API per impostare il profilo come predefinito
            ApiService.put(`/api/profili-calcolo/${id}/predefinito`)
                .then(() => {
                    // Ricarica i profili per mostrare le modifiche
                    caricaProfili();
                })
                .catch(error => {
                    console.error('Errore nell\'impostazione del profilo predefinito:', error);
                    alert('Si è verificato un errore nell\'impostazione del profilo predefinito.');
                    mostraLoading(false);
                });
        }
        
        /**
         * Mostra o nasconde l'indicatore di caricamento
         * @param {boolean} show - true per mostrare, false per nascondere
         */
        function mostraLoading(show) {
            if (show) {
                profiliLoading.classList.remove('d-none');
                profiliError.classList.add('d-none');
                profiliEmpty.classList.add('d-none');
            } else {
                profiliLoading.classList.add('d-none');
            }
        }
        
        /**
         * Formatta la periodicità del bollo
         * @param {string} periodicita - Periodicità del bollo (MENSILE, TRIMESTRALE, ecc.)
         * @returns {string} - Periodicità formattata
         */
        function formatPeriodicitaBollo(periodicita) {
            const map = {
                'MENSILE': 'Mensile',
                'TRIMESTRALE': 'Trimestrale',
                'SEMESTRALE': 'Semestrale',
                'ANNUALE': 'Annuale'
            };
            return map[periodicita] || periodicita;
        }
        
        /**
         * Escape HTML per prevenire XSS
         * @param {string} unsafe - Stringa da rendere sicura
         * @returns {string} - Stringa sicura
         */
        function escapeHtml(unsafe) {
            if (!unsafe) return '';
            
            // Creiamo un elemento temporaneo
            const div = document.createElement('div');
            // Impostiamo il testo (non HTML) nell'elemento
            div.textContent = unsafe;
            // Restituiamo l'HTML interno, che sarà automaticamente escaped
            return div.innerHTML;
        }
    }
});