function fetchRequests() {
    fetch('/university-management-1.0-SNAPSHOT/admin/enrollment-requests', {
        credentials: 'include'
    })
    .then(res => res.json())
    .then(requests => {
        // Separate by status
        const pending = requests.filter(r => r.status === 'PENDING');
        const approved = requests.filter(r => r.status === 'APPROVED');
        const denied = requests.filter(r => r.status === 'DENIED');
        const dropped = requests.filter(r => r.status === 'DROPPED');

        // Render Pending
        const tbody = document.querySelector('#requestsTable tbody');
        tbody.innerHTML = '';
        if (pending.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6">No pending requests.</td></tr>';
        } else {
            pending.forEach(req => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${req.requestId}</td>
                    <td>${req.studentName} (ID: ${req.studentId})</td>
                    <td>${req.courseName} (ID: ${req.courseId})</td>
                    <td>${new Date(req.requestDate).toLocaleString()}</td>
                    <td>${req.status}</td>
                    <td>
                        <button class="approve-btn" onclick="processRequest(${req.requestId}, 'approve')">
                            Approve
                        </button>
                        <button class="deny-btn" onclick="processRequest(${req.requestId}, 'deny')">
                            Deny
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }

        // Render Approved
        const approvedTbody = document.querySelector('#approvedRequestsTable tbody');
        approvedTbody.innerHTML = '';
        if (approved.length === 0) {
            approvedTbody.innerHTML = '<tr><td colspan="5">No approved requests.</td></tr>';
        } else {
            approved.forEach(req => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${req.requestId}</td>
                    <td>${req.studentName} (ID: ${req.studentId})</td>
                    <td>${req.courseName} (ID: ${req.courseId})</td>
                    <td>${new Date(req.requestDate).toLocaleString()}</td>
                    <td>${req.status}</td>
                `;
                approvedTbody.appendChild(tr);
            });
        }

        // Render Denied
        const deniedTbody = document.querySelector('#deniedRequestsTable tbody');
        deniedTbody.innerHTML = '';
        if (denied.length === 0) {
            deniedTbody.innerHTML = '<tr><td colspan="5">No denied requests.</td></tr>';
        } else {
            denied.forEach(req => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${req.requestId}</td>
                    <td>${req.studentName} (ID: ${req.studentId})</td>
                    <td>${req.courseName} (ID: ${req.courseId})</td>
                    <td>${new Date(req.requestDate).toLocaleString()}</td>
                    <td>${req.status}</td>
                `;
                deniedTbody.appendChild(tr);
            });
        }

        // Render Dropped
        const droppedTbody = document.querySelector('#droppedRequestsTable tbody');
        droppedTbody.innerHTML = '';
        if (dropped.length === 0) {
            droppedTbody.innerHTML = '<tr><td colspan="5">No dropped requests.</td></tr>';
        } else {
            dropped.forEach(req => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${req.requestId}</td>
                    <td>${req.studentName} (ID: ${req.studentId})</td>
                    <td>${req.courseName} (ID: ${req.courseId})</td>
                    <td>${new Date(req.requestDate).toLocaleString()}</td>
                    <td>${req.status}</td>
                `;
                droppedTbody.appendChild(tr);
            });
        }
    })
    .catch(() => {
        const tbody = document.querySelector('#requestsTable tbody');
        tbody.innerHTML = '<tr><td colspan="6">Error loading requests.</td></tr>';
        const approvedTbody = document.querySelector('#approvedRequestsTable tbody');
        approvedTbody.innerHTML = '<tr><td colspan="5">Error loading approved requests.</td></tr>';
        const deniedTbody = document.querySelector('#deniedRequestsTable tbody');
        deniedTbody.innerHTML = '<tr><td colspan="5">Error loading denied requests.</td></tr>';
        const droppedTbody = document.querySelector('#droppedRequestsTable tbody');
        droppedTbody.innerHTML = '<tr><td colspan="5">Error loading dropped requests.</td></tr>';
    });
}

function processRequest(requestId, action) {
    fetch(`/university-management-1.0-SNAPSHOT/admin/enrollment-requests/${action}`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ requestId })
    })
    .then(res => {
        if (!res.ok) throw new Error('Failed');
        return res.json();
    })
    .then(() => {
        fetchRequests();
    })
    .catch(() => {
        alert('Failed to process request.');
    });
}
 // Check if user is logged in and is admin
 function checkAuth() {
    fetch('/university-management-1.0-SNAPSHOT/check-auth', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (!data.authenticated || data.role !== 'ADMIN') {
            window.location.href = '../login.html';
        } else {
            document.getElementById('userName').textContent = data.name;
        }
    })
    .catch(() => {
        window.location.href = '../login.html';
    });
}

// Logout function
function logout() {
    fetch('/university-management-1.0-SNAPSHOT/logout', {
        method: 'POST',
        credentials: 'include'
    })
    .then(() => {
        window.location.href = '../login.html';
    });
}

// Initial check for authentication
checkAuth();
fetchRequests(); 