import React, { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useToast } from '../../context/ToastContext';
import Card, { CardHeader, CardBody } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import api from '../../services/api';
import {
  AlertCircle, Plus, CheckCircle2, Clock, MapPin, ShieldAlert,
  PhoneCall, Wrench, Building2
} from 'lucide-react';

export const StudentComplaintsPage = () => {
  const { data, refreshData } = useOutletContext();
  const { addToast } = useToast();

  const [showForm, setShowForm] = useState(false);
  const [category, setCategory] = useState('CAMPUS_FACILITIES');
  const [location, setLocation] = useState('');
  const [urgency, setUrgency] = useState('HIGH');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const grievances = data?.grievances || [];

  const handleCreateComplaint = async (e) => {
    e.preventDefault();
    if (!description.trim()) return;

    setSubmitting(true);
    try {
      const res = await api.post('/student/grievances', {
        category,
        department: 'Campus Infrastructure & Maintenance',
        urgency,
        location: location || 'CS-204',
        description: description.trim(),
      });
      addToast(`Grievance ticket #${res.data.ticketNumber} logged successfully.`, 'success');
      setDescription('');
      setLocation('');
      setShowForm(false);
      refreshData();
    } catch (err) {
      addToast('Unable to submit grievance ticket.', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header Card & Actions */}
      <Card>
        <CardHeader
          title="Campus Infrastructure Grievance & Maintenance Helpdesk"
          subtitle="Report classroom equipment issues, laboratory maintenance, or facility grievances"
          action={
            <Button size="sm" icon={Plus} onClick={() => setShowForm(!showForm)}>
              {showForm ? 'Cancel' : 'File Grievance'}
            </Button>
          }
        />
        <CardBody className="p-5">
          {showForm && (
            <form onSubmit={handleCreateComplaint} className="mb-6 p-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] space-y-4">
              <div className="flex items-center gap-2 pb-2 border-b border-[var(--color-border)]">
                <Wrench className="w-4 h-4 text-[var(--color-primary)]" />
                <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--color-foreground)]">
                  Submit Facility Maintenance Request
                </h4>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Issue Category</label>
                  <select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2 text-[var(--color-foreground)]"
                  >
                    <option value="CAMPUS_FACILITIES">Classroom & Lab Facilities</option>
                    <option value="ELECTRICAL_MAINTENANCE">Electrical / AC / Projector</option>
                    <option value="NETWORK_IT">Campus Wi-Fi & IT Systems</option>
                    <option value="HOSTEL_FACILITY">Hostel Accommodation</option>
                    <option value="ACADEMIC_ADMIN">Academic Administrative</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Classroom / Lab Location</label>
                  <Input
                    placeholder="e.g. CS-204, AI-LAB-01"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Urgency Level</label>
                  <select
                    value={urgency}
                    onChange={(e) => setUrgency(e.target.value)}
                    className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2 text-[var(--color-foreground)]"
                  >
                    <option value="HIGH">High (Immediate Action)</option>
                    <option value="URGENT">Critical / Exam Blocking</option>
                    <option value="MEDIUM">Medium Priority</option>
                    <option value="LOW">Low Priority</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Detailed Description of the Issue</label>
                <textarea
                  rows={3}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describe the issue (e.g. Projector HDMI port damaged, AC not cooling, lab terminal 14 network unreachable)..."
                  className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2.5 text-[var(--color-foreground)] focus:ring-1 focus:ring-[var(--color-primary)] outline-none"
                  required
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <Button size="sm" variant="ghost" onClick={() => setShowForm(false)}>
                  Cancel
                </Button>
                <Button size="sm" type="submit" loading={submitting}>
                  Submit Grievance Ticket
                </Button>
              </div>
            </form>
          )}

          {/* Grievance Tickets List */}
          <div className="space-y-3">
            {grievances.length === 0 ? (
              <div className="text-center py-10 text-xs text-[var(--color-muted-foreground)]">
                <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2 opacity-80" />
                <p className="font-semibold text-[var(--color-foreground)]">No active maintenance tickets</p>
                <p className="mt-0.5">All campus facility reports for your account are resolved or clear.</p>
              </div>
            ) : (
              <div className="divide-y divide-[var(--color-border)]">
                {grievances.map((g) => (
                  <div key={g.id} className="py-3.5 flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                    <div className="space-y-1.5 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="font-mono text-xs font-bold text-[var(--color-primary)]">
                          {g.ticketNumber}
                        </span>
                        <Badge
                          variant={
                            g.status === 'RESOLVED' ? 'success' :
                            g.status === 'IN_PROGRESS' ? 'info' : 'warning'
                          }
                          size="sm"
                        >
                          {g.status}
                        </Badge>
                        <Badge variant="neutral" size="sm">
                          {g.category}
                        </Badge>
                        <Badge variant="primary" size="sm">
                          Routed to: {g.assignedTo || (g.routedTo === 'FACULTY_MENTOR' ? 'Class Mentor' : 'Admin')}
                        </Badge>
                      </div>
                      <p className="text-xs text-[var(--color-foreground)] font-medium">
                        {g.description}
                      </p>
                      <p className="text-[10px] text-[var(--color-muted-foreground)] flex items-center gap-2">
                        <span>Location: <strong>{g.location}</strong></span>
                        <span>•</span>
                        <span>Urgency: <strong>{g.urgency}</strong></span>
                        {g.department && (
                          <>
                            <span>•</span>
                            <span>Dept: <strong>{g.department}</strong></span>
                          </>
                        )}
                      </p>

                      {g.resolutionNotes && (
                        <div className="mt-2 p-2.5 rounded-lg bg-emerald-500/10 border border-emerald-500/20 text-[11px] text-emerald-800 dark:text-emerald-300">
                          <span className="font-bold">Mentor/Admin Resolution:</span> {g.resolutionNotes}
                        </div>
                      )}
                    </div>

                    <div className="text-right text-[11px] text-[var(--color-muted-foreground)] shrink-0">
                      <p className="font-mono">{g.createdAt ? g.createdAt.substring(0, 10) : 'Recent'}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </CardBody>
      </Card>

      {/* Emergency Facility Contacts */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] space-y-1 text-xs">
          <p className="font-bold text-[var(--color-foreground)] flex items-center gap-1.5">
            <Building2 className="w-4 h-4 text-[var(--color-primary)]" />
            Campus Estate & Maintenance
          </p>
          <p className="text-[11px] text-[var(--color-muted-foreground)]">Intercom: Ext 401 / 402</p>
          <p className="text-[11px] text-[var(--color-muted-foreground)]">Location: Facilities Block, Ground Floor</p>
        </div>

        <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] space-y-1 text-xs">
          <p className="font-bold text-[var(--color-foreground)] flex items-center gap-1.5">
            <PhoneCall className="w-4 h-4 text-emerald-500" />
            Campus IT & Network Operations
          </p>
          <p className="text-[11px] text-[var(--color-muted-foreground)]">Intercom: Ext 888</p>
          <p className="text-[11px] text-[var(--color-muted-foreground)]">Location: Data Center, Admin Block</p>
        </div>

        <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] space-y-1 text-xs">
          <p className="font-bold text-[var(--color-foreground)] flex items-center gap-1.5">
            <ShieldAlert className="w-4 h-4 text-amber-500" />
            Campus Security & Helpdesk
          </p>
          <p className="text-[11px] text-[var(--color-muted-foreground)]">24x7 Helpline: +91 422 2678000</p>
          <p className="text-[11px] text-[var(--color-muted-foreground)]">Location: Main Campus Gate 1</p>
        </div>
      </div>
    </div>
  );
};

export default StudentComplaintsPage;
