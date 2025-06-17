// Fetch all instructors
function fetchInstructors() {
    fetch('/university-management-1.0-SNAPSHOT/instructors')
        .then(res => res.json())
        .then(data => {
            const tbody = document.querySelector('#instructorsTable tbody');
            tbody.innerHTML = '';
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No instructors found.</td></tr>';
            } else {
                data.forEach(i => {
                    tbody.innerHTML += `<tr>
                        <td>${i.instructorId}</td>
                        <td>${i.firstName}</td>
                        <td>${i.lastName}</td>
                        <td>${i.email}</td>
                        <td>${i.department || ''}</td>
                        <td>
                            <button class="action-btn" onclick="editInstructor(${i.instructorId}, '${i.firstName}', '${i.lastName}', '${i.email}', '${i.department || ''}')">Edit</button>
                            <button class="action-btn delete" onclick="deleteInstructor(${i.instructorId})">Delete</button>
                        </td>
                    </tr>`;
                });
            }
        });
}

// Add new instructor
document.getElementById('instructorForm').onsubmit = function(e) {
    e.preventDefault();
    const form = e.target;
    fetch('/university-management-1.0-SNAPSHOT/instructors', {
        method: 'POST',
        body: new URLSearchParams(new FormData(form))
    }).then(() => {
        form.reset();
        fetchInstructors();
    });
};

// Delete instructor
function deleteInstructor(id) {
    if (confirm('Are you sure you want to delete this instructor?')) {
        fetch('/university-management-1.0-SNAPSHOT/instructors?id=' + id, { method: 'DELETE' })
            .then(() => fetchInstructors());
    }
}

// Edit instructor modal logic
const instructorModal = document.getElementById('editInstructorModal');
const closeInstructorModal = document.getElementById('closeEditInstructorModal');

closeInstructorModal.onclick = function() { 
    instructorModal.style.display = 'none'; 
};

window.onclick = function(event) { 
    if (event.target == instructorModal) instructorModal.style.display = 'none'; 
};

function editInstructor(id, firstName, lastName, email, department) {
    document.getElementById('editInstructorId').value = id;
    document.getElementById('editInstructorFirstName').value = firstName;
    document.getElementById('editInstructorLastName').value = lastName;
    document.getElementById('editInstructorEmail').value = email;
    document.getElementById('editInstructorDepartment').value = department;
    instructorModal.style.display = 'block';
}

// Update instructor
document.getElementById('editInstructorForm').onsubmit = function(e) {
    e.preventDefault();
    const form = e.target;
    const params = new URLSearchParams(new FormData(form));
    params.append('_method', 'PUT');
    fetch('/university-management-1.0-SNAPSHOT/instructors', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        instructorModal.style.display = 'none';
        fetchInstructors();
    })
    .catch(error => {
        console.error('Error updating instructor:', error);
        alert('Error updating instructor. Please try again.');
    });
};

// Logout function
function logout() {
    fetch('/university-management-1.0-SNAPSHOT/logout', {
        method: 'POST',
        credentials: 'include'
    })
    .then(() => {
        window.location.href = 'login.html';
    });
}
function checkAuth() {
    fetch('/university-management-1.0-SNAPSHOT/check-auth', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (!data.authenticated || data.role !== 'ADMIN') {
            window.location.href = 'login.html';
        } else {
            document.getElementById('userName').textContent = data.name;
        }
    })
    .catch(() => {
        window.location.href = 'login.html';
    });
}

// Initial check for authentication
checkAuth();

// Initial load
fetchInstructors(); 