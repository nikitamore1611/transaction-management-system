const params = new URLSearchParams(window.location.search);

const year = params.get("year");
const month = params.get("month");

async function loadReport() {

    if (!year || !month) {
        alert("Report year and month are required.");
        return;
    }

    try {

        const response = await fetch(
            `/api/reports/monthly?year=${year}&month=${month}`,
            {
                method: "GET"
            }
        );

        const data = await response.json();

        if (!response.ok) {
            throw new Error(
                data.error || "Unable to load report"
            );
        }

        displayReport(data);

    } catch (error) {

        console.error("Report error:", error);

        alert(error.message);
    }
}


function displayReport(report) {

    document.getElementById("reportPeriod").textContent =
        `Report Period: ${report.month}/${report.year}`;

    document.getElementById("openingBalance").textContent =
        formatAmount(report.openingBalance);

    document.getElementById("totalCredit").textContent =
        formatAmount(report.totalCredit);

    document.getElementById("totalDebit").textContent =
        formatAmount(report.totalDebit);

    document.getElementById("closingBalance").textContent =
        formatAmount(report.closingBalance);

    document.getElementById("totalTransactions").textContent =
        report.totalTransactions;

    document.getElementById("reportMessage").textContent =
        report.message;

    displayTransactions(report.transactions);
}


function displayTransactions(transactions) {

    const tableBody =
        document.getElementById("reportTransactionTableBody");

    tableBody.innerHTML = "";

    transactions.forEach(transaction => {

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${transaction.transactionDate}</td>
            <td>${transaction.type}</td>
            <td>${formatAmount(transaction.amount)}</td>
            <td>${transaction.reason}</td>
            <td>${formatAmount(transaction.balance)}</td>
        `;

        tableBody.appendChild(row);
    });
}


function formatAmount(amount) {

    return `₹${Number(amount).toFixed(2)}`;
}


document
    .getElementById("downloadPdf")
    .addEventListener("click", function () {

        if (!year || !month) {
            alert("Report year and month are required.");
            return;
        }

        window.location.href =
            `/api/reports/monthly/pdf?year=${year}&month=${month}`;
    });


document
    .getElementById("backToDashboard")
    .addEventListener("click", function () {

        window.location.href = "index.html";
    });


loadReport();